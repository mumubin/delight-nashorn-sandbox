package delight.nashornsandbox;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

import delight.nashornsandbox.exceptions.ScriptCPUAbuseException;
import junit.framework.Assert;

import static org.junit.Assert.assertThrows;

public class TestEngine {
	
	@Test(expected = ScriptException.class)
	public void test() throws ScriptCPUAbuseException, ScriptException {

		NashornSandbox sandbox = NashornSandboxes.create();
		
		Assert.assertEquals(null, sandbox.eval("this.engine.factory"));
		
	}
	

	@Test(expected = ScriptException.class)
	public void test_with_delete() throws ScriptCPUAbuseException, ScriptException {
		
		NashornSandbox sandbox = NashornSandboxes.create();

		sandbox.eval("Object.defineProperty(this, 'engine', {});\n" + "Object.defineProperty(this, 'context', {});");
        sandbox.eval("delete this.__noSuchProperty__;");
		sandbox.eval("delete this.engine; this.engine.factory;");
		sandbox.eval("delete this.engine; this.engine.factory.scriptEngine.compile('var File = Java.type(\"java.io.File\"); File;').eval()");

		Assert.assertEquals(null, sandbox.eval("delete this.engine; this.engine.factory;"));

	}

	@Test
    public void testEngineCleanInjectVar() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

		// Step 2: Write JavaScript code and execute in NashornSandbox
		String script = " myVar = {\"aa\":\"bb\"};\n" +
				"                workflow.log(\"Hello from NashornSandbox\");\n";
		engine.put("myVar", "{\"Name\":{\"a\":\"b\"}}");
		engine.put("workflow", new TestMethodJavascriptInterface());
		//        engine.eval(script, bindings);
		engine.eval(script);

		// Step 3: Get JavaScript variable value from Nashornengine bindings
		System.out.println("Value of myVar: " + engine.get("myVar"));

		engine.put("myVar", null);
		engine.put("workflow", null);
		//        engine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
		assertThrows(ScriptException.class, () -> engine.eval("workflow.log(myVar);"));
	}
	@Test
    public void testSandboxCleanInjectVar() throws ScriptException {
		NashornSandbox sandbox = NashornSandboxes.create();
		String script = "           workflow.log(myVar);\n" +
				"                myVar = {\"aa\":\"bb\"};\n" +
				"                workflow.log(\"Hello from NashornSandbox\");\n";
		sandbox.inject("myVar", "{\"Name\":{\"a\":\"b\"}}");
		sandbox.inject("workflow", new TestMethodJavascriptInterface());
		sandbox.eval(script);

		System.out.println("Value of myVar: " + sandbox.get("myVar"));

		sandbox.inject("myVar", null);
		sandbox.inject("workflow", null);
		assertThrows(ScriptException.class, () -> sandbox.eval("workflow.log(myVar);"));
		//        assertEquals("TypeError: null has no such function \"log\" in <eval> at line number 1", scriptException.getMessage());
	}

	@Test
	public void testSandboxCleanInjectVar2() throws ScriptException {
		NashornSandbox sandbox = NashornSandboxes.create();
		String script = "           workflow.log(myVar);\n" +
				"                myVar = {\"aa\":\"bb\"};\n" +
				"                workflow.log(\"Hello from NashornSandbox\");\n";
		sandbox.inject("myVar", "{\"Name\":{\"a\":\"b\"}}");
		sandbox.inject("workflow", new TestMethodJavascriptInterface());
		sandbox.eval(script);

		System.out.println("Value of myVar: " + sandbox.get("myVar"));

		sandbox.cleanBinding();
		assertThrows(ScriptException.class, () -> sandbox.eval("workflow.log(myVar);"));
		//        assertEquals("TypeError: null has no such function \"log\" in <eval> at line number 1", scriptException.getMessage());
	}
}
