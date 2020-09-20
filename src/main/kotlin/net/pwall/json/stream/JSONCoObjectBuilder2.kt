package net.pwall.json.stream

import net.pwall.json.JSONConfig
import net.pwall.json.JSONDeserializerFunctions.findParameterName
import net.pwall.json.JSONException
import net.pwall.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

class JSONCoObjectBuilder2(private val path: String, private val targetType: KType, private val config: JSONConfig) :
        JSONCoBuilder  {

    private val targetClass = targetType.classifier as? KClass<*> ?:
            throw JSONException("$path: Can't deserialize $targetType")

    private val results = LinkedHashMap<String, JSONCoBuilder>()
    private val objectProcessor = JSONObjectCoProcessor(path, targetType, config, true) {
        results[it.first] = it.second
    }

    override val complete: Boolean
        get() = objectProcessor.complete

    override val rawValue: Any
        get() = createObject()

    override val jsonValue: JSONObject
        get() {
            val result = JSONObject(results.size)
            for (entry in results.entries)
                result[entry.key] = entry.value.jsonValue
            return result
        }

    override suspend fun acceptChar(ch: Int): Boolean {
        objectProcessor.acceptInt(ch)
        return true
    }

    private fun createObject(): Any {
        try {
            if (targetClass.isSubclassOf(Map::class)) { // TODO is this the best way of testing this?
                val result = LinkedHashMap<String, Any?>(results.size)
                for (entry in results.entries)
                    result[entry.key] = entry.value.rawValue
                return result
            }
            // TODO instantiate object...
            // TODO add code for sealed classes here
            findBestConstructor(targetClass.constructors)?.let { constructor ->
                val argMap = HashMap<KParameter, Any?>()
                for (parameter in constructor.parameters) {
                    val paramName = findParameterName(parameter, config)
                    if (config.hasIgnoreAnnotation(parameter.annotations))
                        results.remove(paramName)
                    else {
                        results[paramName]?.let {
                            argMap[parameter] = it.rawValue
                            results.remove(paramName)
                        }
                    }
                }
                return setRemainingFields(constructor.callBy(argMap))
            }
        }
        catch (e: JSONException) {
            throw e
        }
        catch (e: Exception) {
            throw JSONException("$path: Can't deserialize object as $targetClass", e)
        }
        throw JSONException("$path: Can't deserialize object as $targetClass")
    }

    private fun <T: Any> setRemainingFields(instance: T): T {
        for (entry in results.entries) {
            val member = findField(entry.key)
            if (member != null) {
                if (!config.hasIgnoreAnnotation(member.annotations)) {
                    val value = entry.value.rawValue
                    if (member is KMutableProperty<*>) {
                        val wasAccessible = member.isAccessible
                        member.isAccessible = true
                        try {
                            member.setter.call(instance, value)
                        }
                        catch (e: Exception) {
                            throw JSONException(
                                    "$path: Error setting property ${entry.key} in ${targetClass.simpleName}", e)
                        }
                        finally {
                            member.isAccessible = wasAccessible
                        }
                    }
                    else {
                        if (member.getter.call(instance) != value)
                            throw JSONException("$path: Can't set property ${entry.key} in ${targetClass.simpleName}")
                    }
                }
            }
            else {
                if (!(config.allowExtra || config.hasAllowExtraPropertiesAnnotation(targetClass.annotations)))
                    throw JSONException("$path: Can't find property ${entry.key} in ${targetClass.simpleName}")
            }
        }
        return instance
    }

    private fun findField(name: String): KProperty<*>? {
        for (member in targetClass.members)
            if (member is KProperty<*> && (config.findNameFromAnnotation(member.annotations) ?: member.name) == name)
                return member
        return null
    }

    private fun <T: Any> findBestConstructor(constructors: Collection<KFunction<T>>): KFunction<T>? {
        var result: KFunction<T>? = null
        var best = -1
        for (constructor in constructors) {
            val parameters = constructor.parameters
            if (parameters.any { findParameterName(it, config) == null || it.kind != KParameter.Kind.VALUE })
                continue
            val n = findMatchingParameters(parameters)
            if (n > best) {
                result = constructor
                best = n
            }
        }
        return result
    }

    private fun findMatchingParameters(parameters: List<KParameter>): Int {
        var n = 0
        for (parameter in parameters) {
            if (results.containsKey(findParameterName(parameter, config)))
                n++
            else {
                if (!parameter.isOptional)
                    return -1
            }
        }
        return n
    }

}
