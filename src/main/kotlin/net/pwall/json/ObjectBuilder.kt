package net.pwall.json

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

// ObjectProducer? ObjectCreator? (I'm getting sick of the term "builder")
class ObjectBuilder<T: Any>(val targetType: KType, private val properties: MutableMap<String, Property<*>>,
        val config: JSONConfig) {

    @Suppress("UNCHECKED_CAST")
    private val  targetClass: KClass<T> = targetType.classifier as? KClass<T> ?:
            throw IllegalArgumentException("Can't create $targetType")

    fun create(): Any {
        if (targetClass.isSubclassOf(Map::class)) {
            // TODO create Map
        }
        if (targetClass.isSealed) {
            // TODO handle sealed classes
        }
        targetClass.objectInstance?.let {
            processUnusedFields(it)
            return it
        }

        findBestConstructor()?.let { constructor ->
            val argMap = HashMap<KParameter, Any?>(constructor.parameters.size)
            for (parameter in constructor.parameters) {
                // TODO see new handling of missing nullable arguments in JSONDeserializer
                val paramName = findParameterName(parameter)
                if (config.hasIgnoreAnnotation(parameter.annotations))
                    properties.remove(paramName)
                else {
                    properties[paramName]?.let {
                        argMap[parameter] = it.supplier()
                        properties.remove(paramName)
                    }
                }
            }
            val result = constructor.callBy(argMap)
            processUnusedFields(result)
            return result
        }
        throw JSONException("Can't create object as $targetType")
    }

    private fun createMap(): Map<String, Any?> = HashMap<String, Any?>().apply {
        for ((key, value) in properties.entries)
            this[key] = value.supplier()
    }

    private fun findBestConstructor(): KFunction<T>? {
        var result: KFunction<T>? = null
        var best = -1
        for (constructor in targetClass.constructors) {
            val parameters = constructor.parameters
            if (parameters.any { findParameterName(it) == null || it.kind != KParameter.Kind.VALUE })
                continue
            val n = findMatchingParameters(parameters)
            if (n > best) {
                result = constructor
                best = n
            }
        }
        return result
    }

    private fun findParameterName(parameter: KParameter): String? =
            config.findNameFromAnnotation(parameter.annotations) ?: parameter.name

    private fun findMatchingParameters(parameters: List<KParameter>): Int {
        var n = 0
        for (parameter in parameters) {
            if (properties.containsKey(findParameterName(parameter))) // TODO check property is assignable to parameter?
                n++
            else {
                if (!(parameter.isOptional || parameter.type.isMarkedNullable))
                    return -1
            }
        }
        return n
    }

    private fun processUnusedFields(instance: T) {
        for ((key, value) in properties.entries) {
            val member = findField(key)
            if (member != null) {
                if (!config.hasIgnoreAnnotation(member.annotations)) {
                    val propertyValue = value.supplier()
                    if (member is KMutableProperty<*>) {
                        val wasAccessible = member.isAccessible
                        member.isAccessible = true
                        try {
                            member.setter.call(instance, value)
                        }
                        catch (e: Exception) {
                            throw JSONException("Error setting property $key in ${targetClass.simpleName}", e)
                        }
                        finally {
                            member.isAccessible = wasAccessible
                        }
                    }
                    else {
                        if (member.getter.call(instance) != propertyValue)
                            throw JSONException("Can't set property $key in ${targetClass.simpleName}")
                    }
                }
            }
            else {
                if (!(config.allowExtra || config.hasAllowExtraPropertiesAnnotation(targetClass.annotations)))
                    throw JSONException("Can't find property $key in ${targetClass.simpleName}")
            }
        }
    }

    private fun findField(name: String): KProperty<*>? {
        for (member in targetClass.members)
            if (member is KProperty<*> && (config.findNameFromAnnotation(member.annotations) ?: member.name) == name)
                return member
        return null
    }

    data class Property<T: Any>(val targetClass: KClass<T>, val supplier: () -> T)

    interface Property1<T: Any> { // TODO use this instead of data class?
        val targetClass: KClass<T>
        val value: T
    }

}
