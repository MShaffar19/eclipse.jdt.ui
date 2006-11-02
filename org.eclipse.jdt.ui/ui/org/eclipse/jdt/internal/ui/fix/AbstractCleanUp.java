/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.fix;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.core.resources.ProjectScope;

import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil;

public abstract class AbstractCleanUp implements ICleanUp {

	protected static IDialogSettings getSection(IDialogSettings settings, String sectionName) {
		IDialogSettings section= settings.getSection(sectionName);
		if (section == null)
			section= settings.addNewSection(sectionName);
		return section;
	}

	private static final String SETTINGS_FLAG_NAME= "flag"; //$NON-NLS-1$
	
	private final boolean fOptionsFromPreferences;
	private int fFlags;
	private final Map fOptions;
	
	protected AbstractCleanUp(IDialogSettings settings, int defaultFlag) {

		if (settings.get(SETTINGS_FLAG_NAME) == null)
			settings.put(SETTINGS_FLAG_NAME, defaultFlag);
		
		fFlags= settings.getInt(SETTINGS_FLAG_NAME);
		fOptionsFromPreferences= false;
		fOptions= null;
	}

	protected AbstractCleanUp(int flag) {
		fFlags= flag;
		fOptionsFromPreferences= false;
		fOptions= null;
	}
	
	/**
	 * The clean up does use the <code>options</code>
	 * Does use aliasing
	 */
	protected AbstractCleanUp(Map options) {
		fOptions= options;
		fOptionsFromPreferences= false;
	}
	
	/**
	 * The clean up does load the options from the 
	 * preference on <code>checkPreConditions</code> 
	 */
	protected AbstractCleanUp() {
		fOptions= null;
		fOptionsFromPreferences= true;
		loadSettings(null);
	}
    
	public void saveSettings(IDialogSettings settings) {
		settings.put(SETTINGS_FLAG_NAME, getFlags());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFlag(int flag, boolean b) {
		if (!isFlag(flag) && b) {
			fFlags |= flag;
		} else if (isFlag(flag) && !b) {
			fFlags &= ~flag;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFlag(int flag) {
		return (getFlags() & flag) != 0;
	}
	
	protected int getNumberOfProblems(IProblem[] problems, int problemId) {
		int result= 0;
		for (int i=0;i<problems.length;i++) {
			if (problems[i].getID() == problemId)
				result++;
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkPreConditions(IJavaProject project, ICompilationUnit[] compilationUnits, IProgressMonitor monitor) throws CoreException {
		
		if (fOptionsFromPreferences) {
			loadSettings(project);
		}
		
		if (monitor != null)
			monitor.done();
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkPostConditions(IProgressMonitor monitor) throws CoreException {
		if (monitor != null)
			monitor.done();
		//Default do nothing
		return new RefactoringStatus();
	}
	
    private int getFlags() {
    	if (fOptions != null)
    		return createFlag(fOptions);
    	
	    return fFlags;
    }
    
    protected abstract int createFlag(Map options);

    private void loadSettings(IJavaProject project) {    	
    	IScopeContext context;
    	if (project != null) {
    		context= new ProjectScope(project.getProject());
    	} else {
			context= new InstanceScope();
    	}
    	
		fFlags= createFlag(CleanUpPreferenceUtil.loadOptions(context));
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean needsFreshAST(CompilationUnit compilationUnit) {
        return false;
    }
}
