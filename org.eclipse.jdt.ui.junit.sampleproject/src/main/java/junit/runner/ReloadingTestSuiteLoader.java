package junit.runner;

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
 * A TestSuite loader that can reload classes.
 */
public class ReloadingTestSuiteLoader implements TestSuiteLoader {

	public Class load(String suiteClassName) throws ClassNotFoundException {
		return createLoader().loadClass(suiteClassName, true);
	}

	public Class reload(Class aClass) throws ClassNotFoundException {
		return createLoader().loadClass(aClass.getName(), true);
	}

	protected TestCaseClassLoader createLoader() {
		return new TestCaseClassLoader();
	}
}
