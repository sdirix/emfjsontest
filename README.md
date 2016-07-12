# emfjsontest

Includes testcases for the EMF -> JSON -> EMF round trip

## Setup
 * Use latest Eclipse Modeling Edition
 * Install [emfjson](http://emfjson.org/)
     * Via Marketplace (Does not include sources)
     * Via P2: http://ghillairet.github.io/p2/ (SDK includes Sources)

## Execute
 * Execute the testcases as ```JUnit Plug-in Test```
 * Make sure you only have one ```org.emfjson.jackson``` bundle in your run configuration
 * Remove the ```tearDown``` method to keep all generated files

## Current Problems
 * Parsing to JSON serialized Ecore EEnums leads to a ```java.lang.OutOfMemoryError: GC overhead limit exceeded``` exception. There seems to be a problem with the [EObjectDeserializer](https://github.com/emfjson/emfjson-jackson/blob/master/src/main/java/org/emfjson/jackson/databind/deser/EObjectDeserializer.java) of emfjson. Tip: Debug to the first creation of [JSONException](https://github.com/emfjson/emfjson-jackson/blob/master/src/main/java/org/emfjson/jackson/errors/JSONException.java). To reproduce, replace the task.ecore with the original task.ecore and regenerate the model code. Then execute the ```reloadTaskJson()``` testcase.
 * The round trip (converting an Ecore to Json and back) changes the references. Example: ```eType="#//User``` becomes ```eType="ecore:EClass testoutput/taskEcore.ecore#//User"```. See the ```convertToJsonToEcore()``` testcase.

## Useful Links
 * [emfjson website](http://emfjson.org/) and [emfjson docs](http://emfjson.org/docs/)
 * [emfjson examples repository](https://github.com/emfjson/emfjson-samples)
 * [emfjson github organisation](https://github.com/emfjson/) and [emfjson jackson repository](https://github.com/emfjson/emfjson-jackson)
 * [gson repository](https://github.com/google/gson)
