package burrow.carton.core

import burrow.kernel.furnishing.FurnishingClass
import burrow.kernel.furnishing.annotation.Furniture
import burrow.kernel.furnishing.annotation.RequiredDependencies

object Furnishings {
    fun getFurnitureAnnotation(furnishingClass: FurnishingClass): Furniture =
        furnishingClass.java.getAnnotation(Furniture::class.java)

    fun extractId(furnishingClass: FurnishingClass): String =
        furnishingClass.java.name

    fun extractVersion(furnishingClass: FurnishingClass): String {
        return getFurnitureAnnotation(furnishingClass).version
    }

    fun extractDescription(furnishingClass: FurnishingClass): String {
        return getFurnitureAnnotation(furnishingClass).description
    }

    fun extractType(furnishingClass: FurnishingClass): String {
        return getFurnitureAnnotation(furnishingClass).type
    }

//    fun extractDependencies(furnishingClass: FurnishingClass): List<FurnishingClass> {
//        val dependsOn =
//            furnishingClass.java.getAnnotation(RequiredDependencies::class.java)
//        return dependsOn?.dependencies?.toList() ?: emptyList()
//    }
}