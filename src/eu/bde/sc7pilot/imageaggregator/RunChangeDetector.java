package eu.bde.sc7pilot.imageaggregator;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class RunChangeDetector {
	private String scriptPath;
	
	//public static void main(String[] args) throws IOException {
	//	RunChangeDetector ch=new RunChangeDetector("test.sh");
	//	ch.runchangeDetector();
	//}
	public RunChangeDetector(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public String  runchangeDetector()throws IOException {
		ProcessBuilder pb = new ProcessBuilder(scriptPath, "myArg1", "myArg2");
		pb.redirectErrorStream(true);
		
		//Returns a string map view of this process builder's environment. Whenever a process builder is created, \
		//the environment is initialized to a copy of the current process environment (see System.getenv()).
		//Subprocesses subsequently started by this object's start() method will use this map as their environment. 
		//Map<String, String> env = pb.environment();
//		env.put("VAR1", "myValue");
//		env.remove("OTHERVAR");
//		env.put("VAR2", env.get("VAR1") + "suffix");
		
		//Sets this process builder's working directory. 
		//Subprocesses subsequently started by this object's start() method will use this as their working directory. 
		//The argument may be null -- this means to use the working directory of the current 
		//Java process, usually the directory named by the system property user.dir,
		//as the working directory of the child process.
		//pb.directory(new File("myDir"));
		Process p = pb.start();
		StringWriter writer = new StringWriter();
		IOUtils.copy(p.getInputStream(), writer, "UTF-8");
		System.out.println( writer.toString());
		return writer.toString();
	}
}
