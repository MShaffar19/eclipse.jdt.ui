/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.dialogs;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.Assert;

import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine;

/**
 * A dialog to select a type from a list of types. The dialog allows
 * multiple selections.
 */
public class MultiMainTypeSelectionDialog extends ElementListSelectionDialog {

	private IRunnableContext fRunnableContext;
	private IJavaSearchScope fScope;
	private int fStyle;
		
	/**
	 * Constructor.
	 */
	public MultiMainTypeSelectionDialog(Shell shell, IRunnableContext context,
		IJavaSearchScope scope, int style, boolean ignoreCase)
	{
		super(shell, new JavaElementLabelProvider(
			JavaElementLabelProvider.SHOW_CONTAINER | 
			JavaElementLabelProvider.SHOW_POSTIFIX_QUALIFICATION |
			JavaElementLabelProvider.SHOW_CONTAINER_QUALIFICATION), 
			ignoreCase, true);

		Assert.isNotNull(context);
		Assert.isNotNull(scope);

		fRunnableContext= context;
		fScope= scope;
		fStyle= style;
	}

	/**
	 *
	 */
	public int open() {
		MainMethodSearchEngine engine= new MainMethodSearchEngine();
		List typesFound= engine.searchMethod(fRunnableContext, fScope, fStyle);

		if (typesFound.size() == 0)
			return CANCEL;
		
		setElements(typesFound);
		setInitialSelection("A"); //$NON-NLS-1$
		return super.open();
	}
	
}