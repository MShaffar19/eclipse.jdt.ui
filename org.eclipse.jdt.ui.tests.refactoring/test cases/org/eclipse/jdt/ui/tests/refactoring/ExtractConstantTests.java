package org.eclipse.jdt.ui.tests.refactoring;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.ui.tests.refactoring.infra.SourceCompareUtil;
import org.eclipse.jdt.ui.tests.refactoring.infra.TextRangeUtil;

import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatusCodes;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractConstantRefactoring;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

public class ExtractConstantTests extends RefactoringTest {

	private static final Class clazz = ExtractConstantTests.class;
	private static final String REFACTORING_PATH = "ExtractConstant/";

	private Object fCompactPref; 
		
	public ExtractConstantTests(String name) {
		super(name);
	} 
	
	protected String getRefactoringPath() {
		return REFACTORING_PATH;
	}
	
	public static Test suite() {
		return new MySetup(new TestSuite(clazz));
	}
	
	private String getSimpleTestFileName(boolean canInline, boolean input){
		String fileName = "A_" + getName();
		if (canInline)
			fileName += input ? "_in": "_out";
		return fileName + ".java"; 
	}
	
	private String getTestFileName(boolean canExtract, boolean input){
		String fileName= TEST_PATH_PREFIX + getRefactoringPath();
		fileName += (canExtract ? "canExtract/": "cannotExtract/");
		return fileName + getSimpleTestFileName(canExtract, input);
	}
	
	private String getFailingTestFileName(){
		return getTestFileName(false, false);
	}
	private String getPassingTestFileName(boolean input){
		return getTestFileName(true, input);
	}

	protected ICompilationUnit createCUfromTestFile(IPackageFragment pack, boolean canExtract, boolean input) throws Exception {
		return createCU(pack, getSimpleTestFileName(canExtract, input), getFileContents(getTestFileName(canExtract, input)));
	}

	protected void setUp() throws Exception {
		super.setUp();
		Hashtable options= JavaCore.getOptions();
		fCompactPref= options.get(JavaCore.FORMATTER_COMPACT_ASSIGNMENT);
		options.put(JavaCore.FORMATTER_COMPACT_ASSIGNMENT, JavaCore.COMPACT);
		JavaCore.setOptions(options);
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		Hashtable options= JavaCore.getOptions();
		options.put(JavaCore.FORMATTER_COMPACT_ASSIGNMENT, fCompactPref);
		JavaCore.setOptions(options);	
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, boolean qualifyReferencesWithConstantName, String constantName, String guessedConstantName) throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), true, true);
		ISourceRange selection= TextRangeUtil.getSelection(cu, startLine, startColumn, endLine, endColumn);
		ExtractConstantRefactoring ref= new ExtractConstantRefactoring(cu, selection.getOffset(), selection.getLength(), 
																									JavaPreferencesSettings.getCodeGenerationSettings());
		RefactoringStatus preconditionResult= ref.checkActivation(new NullProgressMonitor());
		assertTrue("activation was supposed to be successful", preconditionResult.isOK());

		if(!allowLoadtime)
			assertTrue("The selected expression has been erroneously reported to contain references to non-static or non-final fields.", ref.selectionAllStaticFinal());		
		
		ref.setReplaceAllOccurrences(replaceAll);
		ref.setQualifyReferencesWithDeclaringClassName(qualifyReferencesWithConstantName);
		ref.setConstantName(constantName);

		assertEquals("constant name incorrectly guessed", guessedConstantName, ref.guessConstantName());

		RefactoringStatus checkInputResult= ref.checkInput(new NullProgressMonitor());
		assertTrue("precondition was supposed to pass", checkInputResult.isOK());	
		
		performChange(ref.createChange(new NullProgressMonitor()));
		
		IPackageFragment pack= (IPackageFragment)cu.getParent();
		String newCuName= getSimpleTestFileName(true, true);
		ICompilationUnit newcu= pack.getCompilationUnit(newCuName);
		assertTrue(newCuName + " does not exist", newcu.exists());
		SourceCompareUtil.compare(newcu.getSource(), getFileContents(getTestFileName(true, false)));
	}

	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName, String guessedConstantName) throws Exception{	
		helper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, false, constantName, guessedConstantName);
	}
	
	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, boolean qualifyReferencesWithConstantName, String constantName) throws Exception{	
		helper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, qualifyReferencesWithConstantName, constantName, constantName);
	}
	
	private void helper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName) throws Exception{
		helper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, false, constantName);
	}	
	
	private void failHelper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName) throws Exception {
		failHelper1(startLine, startColumn, endLine, endColumn, replaceAll, allowLoadtime, constantName, 0, false);	
	}
	private void failHelper1(int startLine, int startColumn, int endLine, int endColumn, boolean replaceAll, boolean allowLoadtime, String constantName, int errorCode, boolean checkCode) throws Exception{
		ICompilationUnit cu= createCUfromTestFile(getPackageP(), false, true);
		ISourceRange selection= TextRangeUtil.getSelection(cu, startLine, startColumn, endLine, endColumn);
		ExtractConstantRefactoring ref= new ExtractConstantRefactoring(cu, selection.getOffset(), selection.getLength(), 
																									JavaPreferencesSettings.getCodeGenerationSettings());
		ref.setReplaceAllOccurrences(replaceAll);
		ref.setConstantName(constantName);
		RefactoringStatus result= performRefactoring(ref);
		
		if(!allowLoadtime && !ref.selectionAllStaticFinal())
			return;
			
		assertNotNull("precondition was supposed to fail", result);
		if(checkCode)
			assertEquals(errorCode, result.getFirstEntry(RefactoringStatus.ERROR).getCode());
	}

	//--- TESTS
	
	public void test0() throws Exception {
		helper1(5, 16, 5, 17, true, false, "CONSTANT");
	}
	
	public void test1() throws Exception {
		helper1(5, 16, 5, 21, false, false, "CONSTANT");	
	}
	
	public void test2() throws Exception {
		helper1(8, 16, 8, 27, false, false, "CONSTANT");
	}
	
	public void test3() throws Exception {
		helper1(8, 16, 8, 27, true, false, "CONSTANT");	
	}
		
	public void test4() throws Exception {
		helper1(5, 23, 5, 34, true, false, "CONSTANT");
	}
	
	public void test5() throws Exception {
		helper1(11, 20, 11, 26, true, true, "CONSTANT");		
	}	
	
	public void test6() throws Exception {
		helper1(13, 20, 13, 35, true, true, "CONSTANT");		
	}
		
	public void test7() throws Exception {
		helper1(12, 20, 12, 28, true, true, "CONSTANT");		
	}
		
	public void test8() throws Exception {
		helper1(8, 16, 8, 22, true, true, "CONSTANT");	
	}
	
	public void test9() throws Exception {
		helper1(6, 24, 6, 29, true, true, "CONSTANT");	
	}

	public void test10() throws Exception {
		helper1(8, 17, 8, 22, true, true, "CONSTANT");	
	}			
	
	public void test11() throws Exception {
		helper1(10, 37, 10, 43, true, true, "CONSTANT");	
	}
	
	public void test12() throws Exception {
		helper1(9, 19, 9, 24, true, true, "CONSTANT");	
	}
	
	public void test13() throws Exception{
		helper1(9, 16, 9, 28, true, true, "CONSTANT");		
	}
		
	public void test14() throws Exception{
		helper1(10, 28, 10, 44, true, true, "CONSTANT");
	}	

	public void test15() throws Exception{
		helper1(5, 16, 5, 25, true, false, "CONSTANT", "FRED");
	}	

	public void test16() throws Exception{
		helper1(5, 20, 5, 27, true, false, "CONSTANT", "RED");
	}	

	public void test17() throws Exception{
		helper1(5, 16, 5, 35, true, false, "CONSTANT", "YET_ANOTHER_FRED");
	}
	
	public void test18() throws Exception {
		helper1(5, 16, 5, 17, true, false, true, "CONSTANT");	
	}
	
	// -- testing failing preconditions
	public void testFail0() throws Exception{
		failHelper1(8, 16, 8, 21, true, true, "CONSTANT");		
	}
	
	public void testFail1() throws Exception{
		failHelper1(8, 16, 8, 26 , true, true, "CONSTANT");		
	}
	
	public void testFail2() throws Exception{
		failHelper1(9, 20, 9, 21 , true, true, "CONSTANT");	
	}
		
	public void testFail3() throws Exception{
		failHelper1(9, 18, 9, 25, true, true, "CONSTANT");	
	}
		
	public void testFail4() throws Exception{
		failHelper1(6, 16, 6, 20, true, true, "CONSTANT");	
	}
		
	public void testFail5() throws Exception{
		failHelper1(9, 16, 9, 25, true, true, "CONSTANT");	
	}
		
	public void testFail6() throws Exception{
		failHelper1(11, 20, 11, 24, true, true, "CONSTANT");	
	}
		
	public void testFail7() throws Exception{
		failHelper1(11, 20, 11, 34, true, true, "CONSTANT");	
	}
		
	public void testFail10() throws Exception{
		failHelper1(15, 20, 15, 37, true, false, "CONSTANT");	
	}
		
	public void testFail11() throws Exception{
		failHelper1(8, 16, 8, 22, true, false, "CONSTANT");
	}
	
	public void testFail12() throws Exception{
		failHelper1(4, 7, 4, 8, true, true, "CONSTANT", RefactoringStatusCodes.EXPRESSION_NOT_RVALUE, true);
	}
	
	public void testFail13() throws Exception {
		failHelper1(2, 9, 2, 10, true, true, "CONSTANT", RefactoringStatusCodes.EXPRESSION_NOT_RVALUE, true);	
	}
	
	public void testFail14() throws Exception {
		failHelper1(5, 9, 5, 11, true, true, "CONSTANT");	
	}
	
	public void testFail15() throws Exception {
		failHelper1(5, 10, 5, 13, true, true, "CONSTANT");	
	}
}

