package junit.framework;

/*-
 * #%L
 * org.eclipse.jdt.ui.junit.sampleproject
 * %%
 * Copyright (C) 2020 Eclipse Foundation
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

/**
 * A Listener for test progress
 */
public interface TestListener {
	/**
	 * An error occurred.
	 */
	void addError(Test test, Throwable t);

	/**
	 * A failure occurred.
	 */
	void addFailure(Test test, AssertionFailedError t);

	/**
	 * A test ended.
	 */
	void endTest(Test test);

	/**
	 * A test started.
	 */
	void startTest(Test test);
}
