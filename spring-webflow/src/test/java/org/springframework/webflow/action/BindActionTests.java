package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.ParserContextImpl;
import org.springframework.binding.message.Severity;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.expression.DefaultExpressionParserFactory;
import org.springframework.webflow.test.MockRequestContext;

public class BindActionTests extends TestCase {
	private ExpressionParser expressionParser = DefaultExpressionParserFactory.getExpressionParser();
	private ConversionService conversionService = new DefaultConversionService();

	private BindAction action;

	public void testSuccessfulBind() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().put("bindTarget", new BindBean());

		Expression target = expressionParser.parseExpression("bindTarget", new ParserContextImpl()
				.eval(RequestContext.class));
		action = new BindAction(target, expressionParser, conversionService);

		LocalAttributeMap eventData = new LocalAttributeMap();
		eventData.put("stringProperty", "foo");
		eventData.put("integerProperty", "3");
		Event event = new Event(this, "submit", eventData);
		context.setLastEvent(event);

		Event result = action.execute(context);
		assertEquals("success", result.getId());

		BindBean bean = (BindBean) context.getFlowScope().get("bindTarget");
		assertEquals("foo", bean.getStringProperty());
		assertEquals(3, bean.getIntegerProperty());
	}

	public void testBindWithErrors() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().put("bindTarget", new BindBean());

		Expression target = expressionParser.parseExpression("bindTarget", new ParserContextImpl()
				.eval(RequestContext.class));
		action = new BindAction(target, expressionParser, conversionService);

		LocalAttributeMap eventData = new LocalAttributeMap();
		eventData.put("stringProperty", "foo");
		eventData.put("integerProperty", "malformed");
		Event event = new Event(this, "submit", eventData);
		context.setLastEvent(event);

		Event result = action.execute(context);
		assertEquals("error", result.getId());

		BindBean bean = (BindBean) context.getFlowScope().get("bindTarget");
		assertEquals("foo", bean.getStringProperty());
		assertEquals(0, bean.getIntegerProperty());
		assertEquals(1, context.getMessageContext().getMessages().length);
		assertEquals("integerProperty", context.getMessageContext().getMessages()[0].getSource());
		assertEquals(Severity.ERROR, context.getMessageContext().getMessages()[0].getSeverity());
		assertEquals("The 'integerProperty' value is the wrong type", context.getMessageContext().getMessages()[0]
				.getText());
	}

	public static class BindBean {
		private String stringProperty;
		private int integerProperty;

		public String getStringProperty() {
			return stringProperty;
		}

		public void setStringProperty(String stringProperty) {
			this.stringProperty = stringProperty;
		}

		public int getIntegerProperty() {
			return integerProperty;
		}

		public void setIntegerProperty(int integerProperty) {
			this.integerProperty = integerProperty;
		}
	}
}