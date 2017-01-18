package eu.bde.sc7pilot.imageaggregator.changeDetection;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class RunSubset {

	private String scriptPath;
	private String filepath;
	private String dim1;
	private String polygon;
	
	//public static void main(String[] args) throws IOException {
	//	RunChangeDetector ch=new RunChangeDetector("test.sh");
	//	ch.runchangeDetector();
	//}
	public RunSubset(String scriptPath, String filepath, String dim1, String polygon) {
		this.scriptPath = scriptPath;
		this.filepath = filepath;
		this.dim1 = dim1;
		this.polygon = polygon;
	}

	public String  runSubset()throws IOException {
		ProcessBuilder pb = new ProcessBuilder(scriptPath, filepath, dim1, polygon);
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
