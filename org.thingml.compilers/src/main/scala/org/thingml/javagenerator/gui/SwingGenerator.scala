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
/**
 * This code generator targets the SMAc Framework
 * see https://github.com/brice-morin/SMAc
 * @author: Brice MORIN <brice.morin@sintef.no>
 */

//TODO: clean the way names are handled
package org.thingml.javagenerator.gui

import org.thingml.javagenerator.gui.SwingGenerator._
import org.sintef.thingml.constraints.ThingMLHelpers
import org.thingml.model.scalaimpl.ThingMLScalaImpl._
import org.sintef.thingml.resource.thingml.analysis.helper.CharacterEscaper
import scala.collection.JavaConversions._
import scala.util.Random
import java.util.{ArrayList, Hashtable}
import org.sintef.thingml._
import java.util.AbstractMap.SimpleEntry
import java.io.{File, FileWriter, PrintWriter, BufferedReader, InputStreamReader}

object Context {
  val builder = new StringBuilder()
  
  var thing : Thing = _
  var port : Port = _
  var pack : String = _
  
  val debug = false
  
  //TODO: should be replaced by Java keywords
  val keywords = scala.List("implicit","match","requires","type","var","abstract","do","finally","import","object","throw","val","case","else","for","lazy","override","return","trait","catch","extends","forSome","match","package","sealed","try","while","class","false","if","new","private","super","true","with","def","final","implicit","null","protected","this","yield","_",":","=","=>","<-","<:","<%",">:","#","@")
  def protectJavaKeyword(value : String) : String = {
    if(keywords.exists(p => p.equals(value))){
      return "`"+value+"`"
    } 
    else {
      return value
    }
  }

  def firstToUpper(value : String) : String = {
    var result = ""
    if (value.size > 0)
      result += value(0).toUpper
    if (value.size > 1)
      result += value.substring(1, value.length)
    return result
  }
  
  def init {
    builder.clear
    generateHeader()
  }
  
  def generateHeader(builder: StringBuilder = Context.builder) = {
    builder append "/**\n"
    builder append " * File generated by the ThingML IDE\n"
    builder append " * /!\\Do not edit this file/!\\\n"
    builder append " * In case of a bug in the generated code,\n"
    builder append " * please submit an issue on our GitHub\n"
    builder append " **/\n\n"

    builder append "package " + Context.pack + ";\n"

    builder append "import org.thingml.generated.*;\n"

    builder append "import org.thingml.java.*;\n"
    builder append "import org.thingml.java.ext.*;\n"
    
    builder append "import java.awt.Color;\n"
    builder append "import java.awt.Dimension;\n"
    builder append "import java.awt.GridBagConstraints;\n"
    builder append "import java.awt.GridBagLayout;\n"
    builder append "import java.awt.Insets;\n"
    builder append "import java.awt.color.ColorSpace;\n"
    builder append "import java.awt.event.ActionEvent;\n"
    builder append "import java.awt.event.ActionListener;\n"
    builder append "import java.util.*;\n"

    builder append "import javax.swing.JButton;\n"
    builder append "import javax.swing.JComboBox;\n"
    builder append "import javax.swing.JFrame;\n"
    builder append "import javax.swing.JLabel;\n"
    builder append "import javax.swing.JPanel;\n"
    builder append "import javax.swing.JScrollPane;\n"
    builder append "import javax.swing.JTabbedPane;\n"
    builder append "import javax.swing.JTextField;\n"
    builder append "import javax.swing.JTextPane;\n"
    builder append "import javax.swing.text.BadLocationException;\n"
    builder append "import javax.swing.text.Style;\n"
    builder append "import javax.swing.text.StyleConstants;\n"
    builder append "import javax.swing.text.StyleContext;\n"
    builder append "import javax.swing.text.StyledDocument;\n" 
    
    builder append "import java.text.SimpleDateFormat;\n"
    
    builder append "\n"
  }
}

object SwingGenerator {
  implicit def swingGeneratorAspect(self: Thing): ThingSwingGenerator = ThingSwingGenerator(self)
  implicit def swingGeneratorAspect(self: Message): MessageSwingGenerator = MessageSwingGenerator(self)
  implicit def swingGeneratorAspect(self: Type): TypeSwingGenerator = TypeSwingGenerator(self)
  implicit def swingGeneratorAspect(self: Instance): InstanceSwingGenerator = InstanceSwingGenerator(self)
  
  
  def compileAndRun(cfg : Configuration, model: ThingMLModel) {
    new File(System.getProperty("java.io.tmpdir") + "/ThingML_temp/").deleteOnExit

    val code = compileAll(model, "org.thingml.generated.gui")

    val rootDir = System.getProperty("java.io.tmpdir") + "/ThingML_temp/" + cfg.getName
    val outputDir = System.getProperty("java.io.tmpdir") + "/ThingML_temp/" + cfg.getName + "/src/main/java/org/thingml/generated/gui"
    
    val outputDirFile = new File(outputDir)
    outputDirFile.mkdirs
    
    code.foreach{case (thing, (mock, mirror)) =>
        var w = new PrintWriter(new FileWriter(new File(outputDir, Context.firstToUpper(thing.getName()) + "Mock.java")));
        w.println(mock);
        w.close();
        
        w = new PrintWriter(new FileWriter(new File(outputDir, Context.firstToUpper(thing.getName()) + "MockMirror.java")));
        w.println(mirror);
        w.close();
        
        w = new PrintWriter(new FileWriter(new File(outputDir, Context.firstToUpper(thing.getName()) + "Listener.java")));
        var b = new StringBuilder()
        thing.generateListener(b, false)
        w.println(b.toString);
        w.close();
        
        w = new PrintWriter(new FileWriter(new File(outputDir, Context.firstToUpper(thing.getName()) + "ListenerMirror.java")));
        b = new StringBuilder()
        thing.generateListener(b, true)
        w.println(b.toString);
        w.close();
    }    

    javax.swing.JOptionPane.showMessageDialog(null, "Java code generated");
  }
  
  
  def compileAllThingJava(model: ThingMLModel, pack : String): Hashtable[Thing, SimpleEntry[String, String]] = {
    val result = new Hashtable[Thing, SimpleEntry[String, String]]()
    compileAll(model, pack).foreach{case (t, entry) =>
        result.put(t, new SimpleEntry(entry._1, entry._2))
    }
    result
  }
  
  def compileAll(model: ThingMLModel, pack : String): Map[Thing, (String, String)] = {
    Context.pack = pack
    
    var thingMap = Map[Thing, (String, String)]()
    model.allThings.filter{t=> !t.isFragment && t.isMockUp}.foreach {t => 
      val thingCode = compile(t, pack)
      val mirrorCode = compile(t, pack, true)
      thingMap += (t -> ((thingCode, mirrorCode)))
    }
    return thingMap
  }
  
  def compile(t: Thing, pack : String, isMirror : Boolean = false) = {
    Context.thing = t
    Context.init
    t.generateSwing(isMirror = isMirror)
    Context.builder.toString
  }
  
}

case class ThingMLSwingGenerator(self: ThingMLElement) {
  def generateSwing(builder: StringBuilder = Context.builder, isMirror : Boolean = false) {
    // Implemented in the sub-classes
  }
}

case class InstanceSwingGenerator(override val self: Instance) extends ThingMLSwingGenerator(self) {
  val instanceName = self.getType.getName + "_" + self.getName
}

case class ThingSwingGenerator(override val self: Thing) extends ThingMLSwingGenerator(self) {
  
  def generateListener(builder: StringBuilder = Context.builder, isMirror : Boolean = false) {
    builder append "package org.thingml.generated.gui;\n\n"
    builder append "public interface " + Context.firstToUpper(self.getName) + "Listener" + (if (isMirror) "Mirror" else "") + " {\n\n"
    
    var messagesToSend = Map[Port, List[Message]]()
    if (!isMirror) 
      self.allPorts.foreach{p => messagesToSend += (p -> p.getSends.toList)} 
    else 
      self.allPorts.foreach{p => messagesToSend +=(p -> p.getReceives.toList)}
    
    /*var messagesToReceive = Map[Port, List[Message]]()
     if (!isMirror) 
     self.allPorts.foreach{p => messagesToReceive += (p -> p.getReceives.toList)} 
     else 
     self.allPorts.foreach{p => messagesToReceive += (p -> p.getSends.toList)} */
    
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{send =>
          builder append "void on" + Context.firstToUpper(send.getName) + "_via_" + port.getName + "(" + send.getParameters.collect{case p => p.getType.java_type + " " + p.getName}.mkString(", ") + ");\n"
        }
    }
    
    builder append "}\n\n"
  }

  override def generateSwing(builder: StringBuilder = Context.builder, isMirror : Boolean = false) {
    
    var messagesToSend = Map[Port, List[Message]]()
    if (!isMirror) 
      self.allPorts.foreach{p => messagesToSend += (p -> p.getSends.toList)} 
    else 
      self.allPorts.foreach{p => messagesToSend +=(p -> p.getReceives.toList)}
    
    var messagesToReceive = Map[Port, List[Message]]()
    if (!isMirror) 
      self.allPorts.foreach{p => messagesToReceive += (p -> p.getReceives.toList)} 
    else 
      self.allPorts.foreach{p => messagesToReceive += (p -> p.getSends.toList)}
     
    builder append "public class " + Context.firstToUpper(self.getName) + "Mock" + (if (isMirror) "Mirror" else "") + " extends Component implements ActionListener {\n\n"
    
    	
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{send =>          
          send.getParameters.foreach{ p => 
            if (p.getType.isInstanceOf[Enumeration]) {
              builder append "private static final Map<String, " + p.getType.scala_type + "> values_" + p.getType.getName + " = new HashMap<String, " + p.getType.scala_type + ">();\n"
              builder append "static {\n"
              p.getType.asInstanceOf[Enumeration].getLiterals.foreach{l =>
                builder append "values_" + p.getType.getName + ".put(\"" + l.getName.toUpperCase + "\", " + p.getType.getName + "_ENUM" + "." + p.getType.getName.toUpperCase + "_" + l.getName.toUpperCase() + "()" + ");\n"
              }
              builder append "}\n\n"
            }
          }
        }
    }

    builder append "//Message types\n"
    self.allMessages.foreach {
      m => builder append "private final " + Context.firstToUpper(m.getName) + "MessageType " + m.getName + "Type = new " + Context.firstToUpper(m.getName) + "MessageType();\n"
    }

    generatePortDecl()
    builder append "\npublic java.util.List<" + Context.firstToUpper(self.getName) + "Listener" + (if (isMirror) "Mirror" else "") + "> listeners = new java.util.LinkedList<" + Context.firstToUpper(self.getName) + "Listener" + (if (isMirror) "Mirror" else "") + ">();\n\n"

    builder append "private SimpleDateFormat dateFormat = new SimpleDateFormat(\"dd MMM yyy 'at' HH:mm:ss.SSS\");"

    //TODO: manage one tab for each port
    builder append "private JTabbedPane tabbedPane = new JTabbedPane();\n"
    builder append "private JFrame frame;\n"
    messagesToSend.foreach{case (port, messages) =>
      builder append "private JPanel frame_" + port.getName + ";\n"
    }
    builder append "private JTextPane screen;\n"
    builder append "private JButton clearButton;\n"

    builder append "private StyledDocument doc;\n\n"

    builder append "public " + Context.firstToUpper(self.getName) + "Mock" + (if (isMirror) "Mirror" else "") + "(String name){\n"
    builder append "super(name);\n"
    generatePortDef(isMirror = isMirror)
    builder append "init();"
    builder append "}\n\n"

    builder append "@Override\n"
    builder append "public Component buildBehavior() {\n"
    builder append "return null;\n"
    builder append "}\n\n"


    builder append "@Override\n"
    builder append "public void receive(Event event, Port port) {\n"
    builder append "super.receive(event, port);\n"
    builder append "print(event.getType().getName() + \"_via_\" + port.getName(), dateFormat.format(new Date()) + \": \" + event.toString());\n"
    builder append "}\n"

    messagesToSend.foreach{case (port, messages) =>
      messages.foreach{send =>
        builder append "//Attributes related to " + send.getName + " via " + port.getName +"\n"
        builder append "public JButton send" + send.getName + "_via_" + port.getName + ";\n"
        send.getParameters.foreach{ p =>
          if (p.getType.isInstanceOf[Enumeration]) {
            builder append "private JComboBox field" + send.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ ";\n"
          }
          else {
            builder append "private JTextField field" + send.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ ";\n"
          }
        }
    
    //////////////////////////////////////////////////////////////////
	
          builder append "public JButton getSend" + send.getName + "_via_" + port.getName + "() {\n"
          builder append "return send" + send.getName + "_via_" + port.getName + ";\n"
          builder append "}\n\n"
        
          send.getParameters.foreach{ p => 
            if (p.getType.isInstanceOf[Enumeration]) {
              builder append "public JComboBox getField" + send.getName + "_via_" + port.getName + "_" +Context.firstToUpper(p.getName)+ "() {\n"
              builder append "return field" + send.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ ";\n"
              builder append "}\n"
            }
            else {
              builder append "public JTextField getField" + send.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "() {\n"
              builder append "return field" + send.getName + "_via_" + port.getName + "_" +Context.firstToUpper(p.getName)+ ";\n"
              builder append "}\n\n"
            }
          }
        }
    }
    
    builder append "public void disableAll() {\n"
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{send =>
          builder append "send" + send.getName + "_via_" + port.getName + ".setEnabled(false);\n"
        }
    }
    builder append "}\n\n"
    
    builder append "public void enableAll() {\n"
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{send =>
          builder append "send" + send.getName + "_via_" + port.getName + ".setEnabled(true);\n"
        }
    }
    builder append "}\n\n"
    
    builder append "public void print(String id, String data){\n"
    builder append "try {\n"
    builder append "doc.insertString(doc.getLength(), formatForPrint(data), doc.getStyle(id));\n"
    builder append "screen.setCaretPosition(doc.getLength());\n"
    builder append "} catch (BadLocationException ex) {\n"
    builder append "ex.printStackTrace();\n"
    builder append "}\n"
    builder append "}\n\n"
		
    builder append "public void addListener(ActionListener l){\n"
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{msg => 
          builder append "send" + msg.getName + "_via_" + port.getName + ".addActionListener(l);\n"
        }
    }
    builder append "}\n\n"

    builder append "@Override\npublic void start() {}\n\n"

    builder append "private void init(){\n"
    
    builder append "GridBagConstraints c = new GridBagConstraints();\n"
    builder append "c.gridwidth = 1;\n"
    builder append "c.fill = GridBagConstraints.HORIZONTAL;\n"
    builder append "c.insets = new Insets(0,3,0,3);\n"
    
    builder append "clearButton = new JButton(\"Clear Console\");\n"
    
    builder append "c.gridy = 0;\n"
    builder append "c.gridx = 0;\n"
    builder append "frame = new JFrame(\"" + self.getName + " Mock Simulator\");\n"
    builder append "frame.setLayout(new GridBagLayout());\n"
    builder append "frame.add(tabbedPane, c);\n"
    
    messagesToSend.foreach{case (port, messages) =>
        builder append "frame_" + port.getName + " = new JPanel();\n"
        builder append "frame_" + port.getName + ".setLayout(new GridBagLayout());\n"
        //builder append "frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n"
    }		
    
    
			
    
    messagesToSend.foreach{case (port, messages) =>
        var x = 0
        messages.foreach{msg => 
          builder append "//GUI related to " + port.getName + "_via_" + port.getName + " => " + msg.getName + "\n"
          builder append "c.gridy = 0;\n"
          builder append "c.gridx = " + x + ";\n"
          builder append "frame_" + port.getName + ".add(createLabel(\"" + msg.getName + "\"), c);\n"
			
          builder append "c.gridy = 1;\n"
          builder append "c.gridx = " + x + ";\n"
          builder append "frame_" + port.getName + ".add(create" + msg.getName + "_via_" + port.getName + "Panel(), c);\n"
			
          builder append "c.gridy = 2;\n"
          builder append "c.gridx = " + x + ";\n"
          builder append "send" + msg.getName + "_via_" + port.getName + " = createSendButton(\"" + port.getName + " => " + msg.getName + "\");\n"
          builder append "frame_" + port.getName + ".add(send" + msg.getName + "_via_" + port.getName + ", c);\n"
			
          
          builder append "tabbedPane.addTab(\"" + port.getName + "\", frame_" + port.getName + ");\n"
          x = x+1
        }
    }
						
    builder append "c.gridy = 1;\n"
    builder append "c.gridx = 0;\n"
    builder append "c.gridwidth = 1;\n"
    builder append "frame.add(createJTextPane(), c);\n"
			
    builder append "c.gridy = 2;\n"
    builder append "frame.add(clearButton, c);\n"
			
    builder append "frame.pack();\n"
    builder append "clearButton.addActionListener(this);\n"
    builder append "addListener(this);\n"
    builder append "frame.setVisible(true);\n"
    builder append "}\n\n"
	
    builder append "public static JLabel createLabel(String name){\n"
    builder append "return new JLabel(name);\n"
    builder append "}\n\n"
	
    builder append "public static JButton createSendButton(String name){\n"
    builder append "return new JButton(\"send\");\n"
    builder append "}\n\n"
    
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{msg => 
          Context.port = port
          msg.generateSwing(isMirror = isMirror)
        }
    }
       
    builder append "public JScrollPane createJTextPane(){\n"
    builder append "screen = new JTextPane();\n"
    builder append "screen.setFocusable(false);\n"
    builder append "screen.setEditable(false);\n"
    builder append "screen.setAutoscrolls(true);\n"
  
    builder append "JScrollPane editorScrollPane = new JScrollPane(screen);\n"
    builder append "editorScrollPane.setVerticalScrollBarPolicy(\n"
    builder append "JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);\n"
    builder append "editorScrollPane.setPreferredSize(new Dimension(480, 240));\n"
    builder append "editorScrollPane.setMinimumSize(new Dimension(320, 160));\n"
        
    builder append "doc = screen.getStyledDocument();\n"
    builder append "//Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);\n"
            
    val rnd = new Random()
    messagesToReceive.foreach{case (port, messages) =>
        messages.foreach{msg =>
          builder append "Style receive" + msg.getName + "_via_" + port.getName + "Style = doc.addStyle(\"" + msg.getName + "_via_" + port.getName + "\", null);\n"
          builder append "StyleConstants.setBackground(receive" + msg.getName + "_via_" + port.getName + "Style, new Color(" + (255-rnd.nextInt(125)) + ", " + (255-rnd.nextInt(125)) + ", " + (255-rnd.nextInt(125)) + "));\n"
        }        
    }
    builder append "return editorScrollPane;\n"
    builder append "}\n\n"
	
    builder append "private String formatForPrint(String text) {\n"
    builder append "return (text.endsWith(\"\\n\") ? text : text + \"\\n\");\n"
    builder append "}\n\n"

    builder append "@Override\n"
    builder append "public void actionPerformed(ActionEvent ae) {\n"
    builder append "if (ae.getSource() == clearButton){\n"
    builder append "screen.setText(\"\");\n"
    builder append "}\n"		
    messagesToSend.foreach{case (port, messages) =>
        messages.foreach{msg =>
          builder append "else if ( ae.getSource() == getSend" + msg.getName + "_via_" + port.getName + "()) {\n"          
          builder append "send(" + msg.getName + "Type.instantiate("
          builder append (msg.getParameters.collect{ case p =>
              (if (p.getCardinality == null) {
                if (p.getType.isInstanceOf[Enumeration]) {
                  "values_" + p.getType.getName + ".get(getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getSelectedItem().toString())"
                  //"new " + p.getType.java_type + "(getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getSelectedItem().toString())"
                } else {
                   "(" + (if (p.getType.java_type() == "int") "Integer" else Context.firstToUpper(p.getType.java_type())) + ") StringHelper.toObject (" +  p.getType.java_type() + ".class, getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getText())"
                }
              }
              //TODO: this is a quick and dirty hack that only works with Byte[]. We need to refactor this code to make it work with any kind of Arrays
              else {
                "getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getText().getBytes()"
              })
            }.toList).mkString(", ")
          builder append "), port_" + Context.firstToUpper(self.getName) + "_" + port.getName + ");\n"
          
          builder append "for(" + Context.firstToUpper(self.getName) + "Listener" + (if (isMirror) "Mirror" else "") + " l : listeners)\n"
          builder append "l.on" + Context.firstToUpper(msg.getName) + "_via_" + port.getName + "("
          builder append (msg.getParameters.collect{ case p =>
              (if (p.getCardinality == null) {
                if (p.getType.isInstanceOf[Enumeration]) {
                  "values_" + p.getType.getName + ".get(getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getSelectedItem().toString())"
                  //"new " + p.getType.java_type + "(getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getSelectedItem().toString())"
                } else {
                  "(" + (if (p.getType.java_type() == "int") "Integer" else Context.firstToUpper(p.getType.java_type())) + ")StringHelper.toObject (" +  p.getType.java_type() + ".class, getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getText())"
                }
              }
              //TODO: this is a quick and dirty hack that only works with Byte[]. We need to refactor this code to make it work with any kind of Arrays
              else {
                "getField" + msg.getName + "_via_" + port.getName + "_" + Context.firstToUpper(p.getName)+ "().getText().getBytes()"
              })
            }.toList).mkString(", ")
          builder append ");\n"
          
          builder append "}\n"
        }
    }
    builder append "}\n\n"
      
    
    builder append "public static void main(String args[]){\n"
    builder append Context.firstToUpper(self.getName) + "Mock mock = new " + Context.firstToUpper(self.getName) + "Mock(\"" + self.getName() + "\");\n"
    builder append Context.firstToUpper(self.getName) + "MockMirror mockMirror = new " + Context.firstToUpper(self.getName) + "MockMirror(\"" + self.getName() + "_mirror\");\n"
    
    /*self.getPorts.foreach{port =>
      builder append "Channel c_" + port.getName + "_" + port.hashCode + " = new Channel("
      builder append "mock.port_" + Context.firstToUpper(self.getName) + "_" + port.getName + ", " + "mockMirror.port_" + Context.firstToUpper(self.getName) + "_" + port.getName
      builder append ");\n"
    } */
    
    builder append "}\n"
    
    
    builder append "}\n"
      
    
  }
  
  def generatePortDecl(builder: StringBuilder = Context.builder) {
    self.allPorts.foreach{ p => 
      builder append "final Port " + "port_" + Context.firstToUpper(self.getName) + "_" + p.getName + ";\n"
    }

    self.allPorts.foreach{ p =>
      builder append "public Port get" + Context.firstToUpper(p.getName) + "_port(){return port_" + Context.firstToUpper(self.getName) + "_" + p.getName + ";}\n"
    }

  }
  
  def generatePortDef(builder: StringBuilder = Context.builder, isMirror : Boolean = false) {
    self.allPorts.foreach{ p =>
      builder append "final List<EventType> in_" + p.getName + " = new ArrayList<EventType>();\n"
      builder append "final List<EventType> out_" + p.getName + " = new ArrayList<EventType>();\n"
      //TODO: Avoid crappy code
      p.getReceives.foreach{ r => 
        if(!isMirror)
          builder append "in_" + p.getName + ".add(" + r.getName + "Type);\n"
        else
          builder append "out_" + p.getName + ".add(" + r.getName + "Type);\n"
      }
      p.getSends.foreach{ s => 
        if(!isMirror)
          builder append "out_" + p.getName + ".add(" + s.getName + "Type);\n"
        else
          builder append "in_" + p.getName + ".add(" + s.getName + "Type);\n"
      }
      builder append "port_" + Context.firstToUpper(self.getName) + "_" + p.getName + " = new Port(" + (if((p.isInstanceOf[ProvidedPort] && !isMirror) || (p.isInstanceOf[RequiredPort] && isMirror)) "PortType.PROVIDED" else "PortType.REQUIRED") + ", \"" + p.getName + "\", in_" + p.getName() + ", out_" + p.getName() + ");\n"
    }
  }
}

case class MessageSwingGenerator(override val self: Message) extends ThingMLSwingGenerator(self) {

  override def generateSwing(builder: StringBuilder = Context.builder, isMirror : Boolean = false) {
    
    builder append "public JPanel create" + self.getName + "_via_" + Context.port.getName + "Panel(){\n"

    builder append "GridBagConstraints c = new GridBagConstraints();\n"
    builder append "c.fill = GridBagConstraints.HORIZONTAL;\n"
    builder append "c.weightx = 0.5;\n"
		
    builder append "JPanel panel = new JPanel(new GridBagLayout());\n"

    var y = 0
    self.getParameters.foreach{ p => 
      builder append "JLabel label" + p.getName + " = new JLabel();\n"
      builder append "label" + p.getName + ".setText(\"" + p.getName + "\");\n"
      builder append "c.gridx = 0;\n"
      builder append "c.gridy = " + y + ";\n"
      builder append "panel.add(label" + p.getName + ", c);\n"
      
      
      if (p.getType.isInstanceOf[Enumeration]) {
        //builder append p.getType.scala_type + "[] values" + self.getName + Context.firstToUpper(p.getName) + " = {"
        //builder append p.getType.asInstanceOf[Enumeration].getLiterals.collect{case l => p.getType.getName + "_ENUM" + "." + p.getType.getName.toUpperCase + "_" + l.getName.toUpperCase() + "()"}.mkString(", ") + "};\n"
        builder append "field" + self.getName + "_via_" + Context.port.getName + "_" +  Context.firstToUpper(p.getName) + " = new JComboBox(values_" + p.getType.getName + ".keySet().toArray());\n"	
      }
      else {		
        builder append "field" + self.getName + "_via_" + Context.port.getName + "_" + Context.firstToUpper(p.getName) + " = new JTextField();\n"
        builder append "field" + self.getName + "_via_" + Context.port.getName + "_" + Context.firstToUpper(p.getName) + ".setText(\"" + p.getName + "\");\n"
      }
    
      builder append "c.gridx = 1;\n"
      builder append "c.gridy = " + y + "\n;"
      builder append "panel.add(field" + self.getName + "_via_" + Context.port.getName + "_" + Context.firstToUpper(p.getName) + ", c);\n"
      y = y+1
    }		
    builder append "return panel;\n"
    builder append "}\n\n"
  }
}


//TODO: Avoid duplicating code from ScalaGenerator.
case class TypeSwingGenerator(override val self: Type) extends ThingMLSwingGenerator(self) {
  override def generateSwing(builder: StringBuilder = Context.builder, isMirror : Boolean = false) {
    // Implemented in the sub-classes
  }

  def default_value(): String = {
    var res : String = self.getAnnotations.filter {
      a => a.getName == "default_value"
    }.headOption match {
      case Some(a) => 
        a.asInstanceOf[PlatformAnnotation].getValue
      case None => ""
    }
    return res
  }
  
  def java_type(): String = {
    var res : String = self.getAnnotations.filter {
      a => a.getName == "java_type"
    }.headOption match {
      case Some(a) => 
        a.asInstanceOf[PlatformAnnotation].getValue
      case None =>
        println("Warning: Missing annotation java_type or scala_type for type " + self.getName + ", using " + self.getName + " as the Java/Scala type.")
        var temp : String = self.getName
        temp = temp(0).toUpperCase + temp.substring(1, temp.length)
        temp
    }
    return res
  }
  
  def scala_type(): String = {
    var res : String = self.getAnnotations.filter {
      a => a.getName == "scala_type"
    }.headOption match {
      case Some(a) => 
        a.asInstanceOf[PlatformAnnotation].getValue
      case None => 
        java_type
    }
    return res
  }
}