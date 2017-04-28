# JArch - Framework for Java Architects
## What is JArch Framework ?
JArch is a modular framework for Java that help developers to accelerate their work while developing 
application architectures. It contains a bunch of battle tested utilities and solid structures that guides developers 
to perform best practice OOP techniques.

# jarch-core
JArch Framework Core Module
## Features:
1. Generic implementations for equals, hashCode and toString methods<br>
By using [@Identity](src/main/java/org/jarchframework/core/model/Identity.java) and 
[@ToString](src/main/java/org/jarchframework/core/model/ToString.java)
annotations, you can implement any object's equals, hashCode and toString methods just specifying the property names. 
You have to extend from [BaseObject](src/main/java/org/jarchframework/core/model/BaseObject.java) for every object you want to make these annotations work.  

Document other features...

#### Note:
Java 8 is required to build and use this project. 
