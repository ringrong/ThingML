/**
 * Copyright (C) 2014 SINTEF <franck.fleurey@sintef.no>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingml.externalthingplugins.c.posix.dnssd;

import java.util.ArrayList;
import java.util.Map;

import org.sintef.thingml.*;
import org.sintef.thingml.constraints.ThingMLHelpers;
import org.sintef.thingml.helpers.CompositeStateHelper;
import org.sintef.thingml.helpers.ThingMLElementHelper;
import org.thingml.compilers.DebugProfile;
import org.thingml.compilers.c.CCompilerContext;
import org.thingml.compilers.c.CThingImplCompiler;
import org.thingml.externalthingplugins.c.posix.PosixDNSSDExternalThingPlugin;
import org.thingml.externalthingplugins.c.posix.dnssd.utils.DNSSDUtils;

/**
 * Created by vassik on 01.11.16.
 */
public class PosixDNSSDThingImplCompiler extends CThingImplCompiler {

    private PosixDNSSDExternalThingPlugin plugin;

    public PosixDNSSDThingImplCompiler(PosixDNSSDExternalThingPlugin _plugin) {
        plugin = _plugin;
    }

    protected void generateCFunctions(Thing thing, StringBuilder builder, CCompilerContext ctx, DebugProfile debugProfile) {
        super.generateCFunctions(thing, builder, ctx, debugProfile);

        builder.append("// Implementation of the declared prototypes to integrate with DNSSD. " +
                "Generated by " + this.getClass().getSimpleName() + "\n");

        Port port = DNSSDUtils.getDNSSDPort(thing);
        Map<String, Property> propertyMap = DNSSDUtils.getDNSSDProperties(thing);
        if(port == null || propertyMap == null)
            return;

        Message message_failure = DNSSDUtils.getDNSSDSrvPublishFailure(port.getSends());
        Message message_publish = DNSSDUtils.getDNSSDSrvPublishSuccess(port.getSends());
        Message message_unpublish = DNSSDUtils.getDNSSDSrvUnpublishSuccess(port.getSends());

        Map<String, String> success_callback = DNSSDUtils.generateDNSSDClientRunningCallback(thing, ctx);
        Map<String, String> failure_callback = DNSSDUtils.generateDNSSDClientFailureCallback(thing, port, message_failure, ctx);
        Map<String, String> publish_success_callback = DNSSDUtils.generateDNSSDSrvPublishCallback(thing, port, message_publish, ctx);
        Map<String, String> unpublish_success_callback = DNSSDUtils.generateDNSSDSrvUnpublishCallback(thing, port, message_unpublish, ctx);
        Map<String, String> publish_failure_callback = DNSSDUtils.generateDNSSDSrvFailureCallback(thing, port, message_failure, ctx);

        builder.append("// DNSSD callbacks. " +
                "Generated by " + this.getClass().getSimpleName() + "\n\n");
        builder.append(success_callback.values().iterator().next());
        builder.append("\n");
        builder.append(failure_callback.values().iterator().next());
        builder.append("\n");
        builder.append(publish_success_callback.values().iterator().next());
        builder.append("\n");
        builder.append(unpublish_success_callback.values().iterator().next());
        builder.append("\n");
        builder.append(publish_failure_callback.values().iterator().next());
        builder.append("\n");

        builder.append("void ");
        builder.append(thing.getName() + "_setup");
        ctx.appendFormalParametersEmptyHandler(thing, builder);
        builder.append("{\n");
        builder.append(ctx.getInstanceVarName() + "->avahi_client = " +
                plugin.getProtocolName() + "_constructDNSSDThreadedAhvaiClient();\n");
        builder.append(ctx.getInstanceVarName() + "->avahi_client->thing_instance = " +
                "" + ctx.getInstanceVarName() + ";\n");

        builder.append(ctx.getInstanceVarName() + "->avahi_client->fn_client_running_callback = " +
                "" + success_callback.keySet().iterator().next() + ";\n");

        builder.append(ctx.getInstanceVarName() + "->avahi_client->fn_client_failure_callback = " +
                "" + failure_callback.keySet().iterator().next() + ";\n");
        builder.append("}\n\n");


        builder.append("void ");
        builder.append(thing.getName() + "_startup");
        ctx.appendFormalParametersEmptyHandler(thing, builder);
        builder.append("{\n");
        builder.append(plugin.getProtocolName() + "_start_avahi_client" +
                "(" + ctx.getInstanceVarName() + "->avahi_client);\n");
        builder.append("}\n\n");


        builder.append("void ");
        builder.append(thing.getName() + "_shutdown");
        ctx.appendFormalParametersEmptyHandler(thing, builder);
        builder.append("{\n");
        builder.append(plugin.getProtocolName() + "_remove_dnssd_service" +
                "(" + ctx.getInstanceVarName() + "->service_data);\n");
        builder.append(plugin.getProtocolName() + "_distructDNSSDAvahiService" +
                "(&" + ctx.getInstanceVarName() + "->service_data);\n");
        builder.append(plugin.getProtocolName() + "_distructDNSSDThreadedAhvaiClient" +
                "(&" + ctx.getInstanceVarName() + "->avahi_client);\n");
        builder.append("}\n\n");


        builder.append("void ");
        builder.append(thing.getName() + "_add_dnssd_service");
        ctx.appendFormalParametersEmptyHandler(thing, builder);
        builder.append("{\n");
        builder.append(ctx.getInstanceVarName() + "->service_data = " +
                plugin.getProtocolName() + "_constructDNSSDAvahiService();\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->fn_srv_publish_success_callback = " +
                publish_success_callback.keySet().iterator().next() + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->fn_srv_unpublish_success_callback = " +
                unpublish_success_callback.keySet().iterator().next() + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->fn_srv_failure_callback = " +
                publish_failure_callback.keySet().iterator().next() + ";\n");

        builder.append(ctx.getInstanceVarName() + "->service_data->name = " +
                ctx.getInstanceVarName() + "->" +ctx.getCVarName(propertyMap.get(DNSSDUtils.srv_name)) + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->type = " +
                ctx.getInstanceVarName() + "->" +ctx.getCVarName(propertyMap.get(DNSSDUtils.srv_type)) + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->port = " +
                ctx.getInstanceVarName() + "->" +ctx.getCVarName(propertyMap.get(DNSSDUtils.srv_port)) + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->txt = " +
                ctx.getInstanceVarName() + "->" +ctx.getCVarName(propertyMap.get(DNSSDUtils.srv_txt)) + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->host = " +
                ctx.getInstanceVarName() + "->" +ctx.getCVarName(propertyMap.get(DNSSDUtils.srv_host)) + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->domain = " +
                ctx.getInstanceVarName() + "->" + ctx.getCVarName(propertyMap.get(DNSSDUtils.srv_domain)) + ";\n");
        builder.append(ctx.getInstanceVarName() + "->service_data->avahi_client = " +
                ctx.getInstanceVarName() + "->avahi_client;\n");

        builder.append(plugin.getProtocolName() + "_add_dnssd_service" +
                "(" + ctx.getInstanceVarName() + "->service_data);\n");
        builder.append("}\n\n");


        builder.append("void ");
        builder.append(thing.getName() + "_remove_dnssd_service");
        ctx.appendFormalParametersEmptyHandler(thing, builder);
        builder.append("{\n");
        builder.append(plugin.getProtocolName() + "_remove_dnssd_service" +
                "(" + ctx.getInstanceVarName() + "->service_data);\n");
        builder.append(plugin.getProtocolName() + "_distructDNSSDAvahiService" +
                "(&" + ctx.getInstanceVarName() + "->service_data);\n");

        builder.append("}\n\n");
    }


    @Override
    protected void generateEntryActions(Thing thing, StringBuilder builder, CCompilerContext ctx, DebugProfile debugProfile) {
        builder.append("// Custom behavior for the DNSSD thing. " +
                "Generated by " + this.getClass().getSimpleName() + "\n");

        if (ThingMLHelpers.allStateMachines(thing).isEmpty()) return;

        StateMachine sm = ThingMLHelpers.allStateMachines(thing).get(0);


        builder.append("void " + getCppNameScope() + ThingMLElementHelper.qname(sm, "_") + "_OnEntry(int state, ");
        builder.append("struct " + ctx.getInstanceStructName(thing) + " *" + ctx.getInstanceVarName() + ") {\n");

        builder.append("switch(state) {\n");

        //there must be one empty state
        CompositeState cs = CompositeStateHelper.allContainedCompositeStatesIncludingSessions(sm).get(0);

        builder.append("case " + ctx.getStateID(cs) + ":{\n");
        if (debugProfile.isDebugBehavior()) {
            builder.append(thing.getName() + "_print_debug(" + ctx.getInstanceVarName() + ", \""
                    + ctx.traceOnEntry(thing, sm) + "\\n\");\n");
        }
        ArrayList<Region> regions = new ArrayList<Region>();
        regions.add(cs);
        regions.addAll(cs.getRegion());
        // Init state
        for (Region r : regions) {
            if (!r.isHistory()) {
                builder.append(ctx.getInstanceVarName() + "->" + ctx.getStateVarName(r) + " = " + ctx.getStateID(r.getInitial()) + ";\n");
            }
        }
        // Execute Entry actions
        builder.append(thing.getName() + "_setup");
        builder.append("("+ ctx.getInstanceVarName() +");\n");

        // Recurse on contained states
        for (Region r : regions) {
            builder.append(ThingMLElementHelper.qname(sm, "_") + "_OnEntry(" + ctx.getInstanceVarName() + "->" + ctx.getStateVarName(r) + ", " + ctx.getInstanceVarName() + ");\n");
        }

        State s = CompositeStateHelper.allContainedSimpleStatesIncludingSessions(sm).get(0);
        builder.append("break;\n}\n");

        builder.append("case " + ctx.getStateID(s) + ":{\n");

        if (debugProfile.isDebugBehavior()) {
            builder.append(thing.getName() + "_print_debug(" + ctx.getInstanceVarName() + ", \""
                    + ctx.traceOnEntry(thing, sm, s) + "\\n\");\n");
        }

        // Execute Entry actions
        builder.append(thing.getName() + "_startup");
        builder.append("("+ ctx.getInstanceVarName() +");\n");

        builder.append("break;\n}\n");

        builder.append("case " + DNSSDUtils.getTerminateStateName(thing) + ":{\n");

        if (debugProfile.isDebugBehavior()) {
            builder.append(thing.getName() + "_print_debug(" + ctx.getInstanceVarName() + ", \"" +
                    "Enters custom terminate state of DNSSD"
                     + "\\n\");\n");
        }

        // Execute Entry actions
        builder.append(thing.getName() + "_shutdown");
        builder.append("("+ ctx.getInstanceVarName() +");\n");

        builder.append("break;\n}\n");

        builder.append("default: break;\n");
        builder.append("}\n");
        builder.append("}\n");
        
    }

    @Override
    protected void generateStateMachineOnExitCPrototypes(Thing thing, StringBuilder builder, CCompilerContext ctx) {
        builder.append("// No on exit action for DNSSD. " +
                "Generated by " + this.getClass().getSimpleName() + "\n");
    }

    @Override
    protected void generateExitActions(Thing thing, StringBuilder builder, CCompilerContext ctx, DebugProfile debugProfile) {
        builder.append("// No on exit action for DNSSD. " +
                "Generated by " + this.getClass().getSimpleName() + "\n");
    }
}
