/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.search;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.StyledString;

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.ui.JavaElementLabels;


public class SortingLabelProvider extends SearchLabelProvider {

	public static final int SHOW_ELEMENT_CONTAINER= 1; // default
	public static final int SHOW_CONTAINER_ELEMENT= 2;
	public static final int SHOW_PATH= 3;

	private static final long FLAGS_QUALIFIED= DEFAULT_SEARCH_TEXTFLAGS | JavaElementLabels.F_FULLY_QUALIFIED | JavaElementLabels.M_FULLY_QUALIFIED | JavaElementLabels.I_FULLY_QUALIFIED
		| JavaElementLabels.T_FULLY_QUALIFIED | JavaElementLabels.D_QUALIFIED | JavaElementLabels.CF_QUALIFIED  | JavaElementLabels.CU_QUALIFIED | JavaElementLabels.COLORIZE;


	private int fCurrentOrder;

	public SortingLabelProvider(JavaSearchResultPage page) {
		super(page);
		fCurrentOrder= SHOW_ELEMENT_CONTAINER;
	}

	@Override
	public Image getImage(Object element) {
		Image image= null;
		if (element instanceof IJavaElement || element instanceof IResource)
			image= super.getImage(element);
		if (image != null)
			return image;
		return getParticipantImage(element);
	}

	@Override
	public final String getText(Object element) {
		if (element instanceof IImportDeclaration)
			element= ((IImportDeclaration)element).getParent().getParent();

		String text= super.getText(element);
		if (text.length() > 0) {
			String labelWithCount= getLabelWithCounts(element, text);
			if (fCurrentOrder == SHOW_ELEMENT_CONTAINER) {
				labelWithCount += getPostQualification(element);
			}
			return labelWithCount;
		}
		return getParticipantText(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof IImportDeclaration)
			element= ((IImportDeclaration)element).getParent().getParent();

		StyledString text= super.getStyledText(element);
		if (text.length() > 0) {
			StyledString countLabel= getColoredLabelWithCounts(element, text);
			if (fCurrentOrder == SHOW_ELEMENT_CONTAINER) {
				countLabel.append(getPostQualification(element), StyledString.QUALIFIER_STYLER);
			}
			return countLabel;
		}
		return getStyledParticipantText(element);
	}

	private String getPostQualification(Object element) {
		String textLabel= JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_POST_QUALIFIED);
		int indexOf= textLabel.indexOf(JavaElementLabels.CONCAT_STRING);
		if (indexOf != -1) {
			return textLabel.substring(indexOf);
		}
		return ""; //$NON-NLS-1$
	}

	public void setOrder(int orderFlag) {
		fCurrentOrder= orderFlag;
		long flags= 0;
		switch (orderFlag) {
		case SHOW_ELEMENT_CONTAINER:
			flags= DEFAULT_SEARCH_TEXTFLAGS;
			break;
		case SHOW_CONTAINER_ELEMENT:
			flags= FLAGS_QUALIFIED;
			break;
		case SHOW_PATH:
			flags= FLAGS_QUALIFIED | JavaElementLabels.PREPEND_ROOT_PATH;
			break;
		default:
			break;
		}
		setTextFlags(flags);
	}
}
