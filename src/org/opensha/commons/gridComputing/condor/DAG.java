package org.opensha.commons.gridComputing.condor;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class represents a Condor (high throughput computing) DAG.
 * 
 * It has the ability to handle Job dependencies, Pre/Post scripts, job retry,
 * and .dot file generation.
 * 
 * @author kevin
 *
 */
public class DAG {
	
	public static DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	
	private ArrayList<SubmitScriptForDAG> scripts = new ArrayList<SubmitScriptForDAG>();
	private ArrayList<ParentChildRelationship> relationships = new ArrayList<ParentChildRelationship>();
	
	private String globalComments = "";
	
	private String dotFileName = null;
	private boolean dotUpdate = true;
	
	public DAG() {
		
	}
	
	public void addJob(SubmitScriptForDAG script) {
		if (script.getJobName() == null) {
			throw new NullPointerException("Script job name cannot be null!");
		}
		for (SubmitScriptForDAG oldScript : scripts) {
			if (oldScript.getJobName().equals(script.getJobName()))
				throw new IllegalArgumentException("A script already exists in this DAG with the name " +
						"'" + script.getJobName() + "'");
		}
		this.scripts.add(script);
	}
	
	public void addJob(SubmitScriptForDAG script, String comment) {
		addJob(script);
		script.setComment(comment);
	}
	
	public void addParentChildRelationship(ParentChildRelationship relationship) {
		boolean hasParent = false;
		boolean hasChild = false;
		for (SubmitScriptForDAG script : scripts) {
			if (script.getJobName().equals(relationship.getParent().getJobName()))
				hasParent = true;
			if (script.getJobName().equals(relationship.getChild().getJobName()))
				hasChild = true;
			if (hasParent && hasChild)
				break;
		}
		if (!hasParent)
			throw new IllegalArgumentException("Parent job '" + relationship.getParent().getJobName()
					+ " is not yet part of this DAG!");
		if (!hasChild)
			throw new IllegalArgumentException("Child job '" + relationship.getChild().getJobName()
					+ " is not yet part of this DAG!");
		this.relationships.add(relationship);
	}
	
	public void addParentChildRelationship(SubmitScriptForDAG parent, SubmitScriptForDAG child) {
		this.addParentChildRelationship(new ParentChildRelationship(parent, child));
	}
	
	public void addParentChildRelationship(SubmitScriptForDAG parent, SubmitScriptForDAG child,
			String comment) {
		ParentChildRelationship relationship = new ParentChildRelationship(parent, child);
		relationship.setComment(comment);
		this.addParentChildRelationship(relationship);
	}
	
	public String getDAG() {
		String dag = "";
		
		dag += "# Condor DAG automatically generated by " + this.getClass().getCanonicalName() + "\n";
		dag += "# date: " + df.format(new Date()) + "\n";
		dag += "\n";
		if (globalComments != null && globalComments.length() > 0)
			dag += globalComments + "\n";
		
		dag += "\t## Condor Submit Scripts ##\n\n";
		for (SubmitScriptForDAG script : scripts) {
			if (script.hasComment()) {
				String comment = script.getComment();
				checkAddNewline(comment);
				dag += comment;
			}
			String fileName = script.getFileName();
			if (fileName == null || fileName.length() <= 0)
				throw new NullPointerException("Job '" + script.getJobName() + "' has no file name!");
			dag += "JOB " + script.getJobName() + " " + fileName + "\n";
			if (script.hasPreScript()) {
				dag += "SCRIPT PRE " + script.getJobName() + " " + checkAddNewline(script.getPreScript());
			}
			if (script.hasPostScript()) {
				dag += "SCRIPT POST " + script.getJobName() + " " + checkAddNewline(script.getPostScript());
			}
			if (script.hasRetries()) {
				dag += "RETRY " + script.getJobName() + " " + script.getRetries() + "\n";
			}
		}
		
		dag +="\n\t## Parent Child Relationships ##\n\n";
		for (ParentChildRelationship relationship : relationships) {
			if (relationship.hasComment()) {
				String comment = relationship.getComment();
				checkAddNewline(comment);
				dag += comment;
			}
			dag += "PARENT " + relationship.getParent().getJobName()
					+ " CHILD " + relationship.getChild().getJobName() + "\n";
		}
		
		dag += "\n";
		
		if (this.dotFileName != null && dotFileName.length() > 0) {
			dag += "DOT " + dotFileName;
			if (dotUpdate)
				dag += " UPDATE";
			dag += "\n";
		}
		
		dag += "\n## END DAG ##\n";
		
		return dag;
	}
	
	public void writeDag(String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		
		fw.write(getDAG());
		
		fw.close();
	}
	
	public static String checkAddNewline(String line) {
		if (!line.endsWith("\n"))
			line += "\n";
		return line;
	}
	
	public void setGlobalComments(String globalComments) {
		if (globalComments != null && globalComments.length() > 0 && !globalComments.startsWith("#"))
			globalComments = "#" + globalComments;
		this.globalComments = globalComments;
	}
	
	public void setDotFile(String dotFileName) {
		this.dotFileName = dotFileName;
	}
	
	public void setDotFile(String dotFileName, boolean dotUpdate) {
		setDotFile(dotFileName);
		this.dotUpdate = dotUpdate;
	}

}
