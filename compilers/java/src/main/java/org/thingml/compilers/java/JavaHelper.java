/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.thingml.compilers.java;

import org.thingml.xtext.thingML.*;
import org.thingml.xtext.helpers.*;
import org.thingml.compilers.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bmori on 09.12.2014.
 */
public class JavaHelper {

    public static String getDefaultValue(Type type) {
        if (AnnotatedElementHelper.isDefined(type, "java_type", "boolean"))
            return "false";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "int"))
            return "0";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "long"))
            return "0";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "float"))
            return "0.0f";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "double"))
            return "0.0d";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "byte"))
            return "0";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "short"))
            return "0";
        else if (AnnotatedElementHelper.isDefined(type, "java_type", "char"))
            return "'\u0000'";
        else
            return "null";
    }

    public static String getJavaType(final Type type, final boolean isArray, final Context ctx) {
        StringBuilder builder = new StringBuilder();
        if (type == null) {//void
            builder.append("void");
        } else if (type instanceof Enumeration) {//enumeration
            builder.append(ctx.firstToUpper(type.getName()) + "_ENUM");
        } else {
            if (AnnotatedElementHelper.hasAnnotation(type, "java_type")) {
                builder.append(AnnotatedElementHelper.annotation(type, "java_type").toArray()[0]);
            } else {
                System.err.println("WARNING: no Java type defined for ThingML datatype " + type.getName());
                builder.append("/*No Java type was explicitly defined*/ Object");
            }
            if (isArray) {//array
                builder.append("[]");
            }
        }
        return builder.toString();
    }

    public static void generateHeader(String pack, String rootPack, StringBuilder builder, Context ctx, boolean isMain, boolean hasMessages, boolean hasStream) {
        builder.append("/**\n");
        builder.append(" * File generated by the ThingML IDE\n");
        builder.append(" * /!\\Do not edit this file/!\\\n");
        builder.append(" * In case of a bug in the generated code,\n");
        builder.append(" * please submit an issue on our GitHub\n");
        builder.append(" **/\n\n");

        builder.append("package " + pack + ";\n\n");

        builder.append("import org.thingml.java.*;\n");
        builder.append("import org.thingml.java.ext.*;\n\n");
        builder.append("import " + rootPack + ".api.*;\n");

        if (hasMessages)
            builder.append("import " + rootPack + ".messages.*;\n\n");

        if (hasStream) {
            builder.append("import rx.functions.Action1;\n" +
                    "import rx.subjects.PublishSubject;" +
                    "import rx.functions.Func1;\n" +
                    "import rx.functions.Func2;" +
                    "import java.util.concurrent.TimeUnit;");
        }

        builder.append("import java.util.*;\n");
    }

    public static void generateParameter(Message m, StringBuilder builder, Context ctx) {
        int i = 0;
        for (Parameter pa : m.getParameters()) {
            if (i > 0)
                builder.append(", ");
            builder.append(JavaHelper.getJavaType(pa.getTypeRef().getType(), pa.getTypeRef().isIsArray(), ctx) + " " + ctx.protectKeyword(ctx.getVariableName(pa)));
            i++;
        }
    }

    public static void generateParameter(Function m, StringBuilder builder, Context ctx) {
        int i = 0;
        for (Parameter pa : m.getParameters()) {
            if (i > 0)
                builder.append(", ");
            builder.append("final " + JavaHelper.getJavaType(pa.getTypeRef().getType(), pa.getTypeRef().isIsArray(), ctx) + " " + ctx.protectKeyword(ctx.getVariableName(pa)));
            i++;
        }
    }

    public static void generateParameter(Session s, StringBuilder builder, Context ctx) {
        int i = 0;
        for (Property pa : s.getProperties()) {
            if (i > 0)
                builder.append(", ");
            builder.append(JavaHelper.getJavaType(pa.getTypeRef().getType(), pa.getTypeRef().isIsArray(), ctx) + " " + ctx.protectKeyword(ctx.getVariableName(pa)) + "_");
            i++;
        }
    }

    /**
     *
     * @return
     * @generated NOT
     */
    public static Set<String> allThingMLMavenDep(Configuration cfg) {//FIXME: should be moved in a JavaHelper
        Set<String> result = new HashSet<String>();
        for (Thing t : ConfigurationHelper.allThings(cfg)) {
            for (String dep : AnnotatedElementHelper.annotation(t, "thingml_maven_dep")) {
                String cleanDep = dep.replace(" ", "").replace("\n", "").replace("\t", "");
                result.add(cleanDep);
            }
        }
        return result;
    }

    /**
     *
     * @return
     * @generated NOT
     */
    public static Set<String> allMavenDep(Configuration cfg) {//FIXME: should be moved in a JavaHelper
        Set<String> result = new HashSet<String>();
        for (Thing t : ConfigurationHelper.allThings(cfg)) {
            for (String dep : AnnotatedElementHelper.annotation(t, "maven_dep")) {
                String cleanDep = dep.replace(" ", "").replace("\n", "").replace("\t", "");
                result.add(cleanDep);
            }
        }
        return result;
    }
}
