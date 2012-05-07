/**
 * Copyright 2010-2012 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ralscha.extdirectspring.controller;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.ralscha.extdirectspring.bean.ExtDirectResponse;
import ch.ralscha.extdirectspring.provider.FormInfo;
import ch.ralscha.extdirectspring.provider.Row;

/**
 * Tests for {@link RouterController}.
 * 
 * @author Ralph Schaer
 */
@SuppressWarnings("all")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testApplicationContext.xml")
public class RouterControllerSimpleTest {

	@Autowired
	private RouterController controller;

	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@Before
	public void beforeTest() {
		Locale.setDefault(Locale.US);
		response = new MockHttpServletResponse();
		request = new MockHttpServletRequest();
	}

	@Test
	public void testBeanNotFound() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProvider", "method1", 1, 3, 2.5,
				"string.param");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		checkBeanNotFoundResponse(responses.get(0));
	}

	static void checkBeanNotFoundResponse(ExtDirectResponse resp) {
		assertThat(resp.getAction()).isEqualTo("remoteProvider");
		assertThat(resp.getMethod()).isEqualTo("method1");
		assertThat(resp.getType()).isEqualTo("exception");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getResult()).isNull();
		assertThat(resp.getMessage()).isEqualTo("Server Error");
		assertThat(resp.getWhere()).isNull();
	}

	@Test
	public void testMethodNotFound() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method4", 2, 3, 2.5,
				"string.param");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		checkMethodNotFoundResponse(responses.get(0));

	}

	static void checkMethodNotFoundResponse(ExtDirectResponse resp) {
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method4");
		assertThat(resp.getType()).isEqualTo("exception");
		assertThat(resp.getTid()).isEqualTo(2);
		assertThat(resp.getResult()).isNull();
		assertThat(resp.getMessage()).isEqualTo("Server Error");
		assertThat(resp.getWhere()).isNull();
	}

	@Test
	public void testNoParameters() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method1", 1, null);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		checkNoParametersResponse(responses.get(0), 1);
	}

	static void checkNoParametersResponse(ExtDirectResponse resp, int tid) {
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method1");
		assertThat(resp.getTid()).isEqualTo(tid);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo("method1() called");
	}

	@Test
	public void testNoParametersWithRequestParameter() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method1", 1,
				"requestparameter");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		checkNoParametersResponse(resp, 1);
	}

	@Test
	public void testNoParameters2() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method2", 1, null);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method2");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo("method2() called");
	}

	@Test
	public void testWithParameters() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method3", 10, 1, 3.1,
				"requestParameter");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method3");
		assertThat(resp.getTid()).isEqualTo(10);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo("method3() called-1-3.1-requestParameter");
	}

	@Test
	public void testWithParametersWithTypeConversion() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method3", 10, "10",
				"4.2", 20);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method3");
		assertThat(resp.getTid()).isEqualTo(10);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo("method3() called-10-4.2-20");
	}

	@Test
	public void testWithParametersNoRequestParameter() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method3", 1, null);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method3");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("exception");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isEqualTo("Server Error");
		assertThat(resp.getResult()).isNull();
	}

	@Test
	public void testResultTrue() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method5", 1, "ralph");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method5");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo(true);
	}

	@Test
	public void testResultFalse() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method5", 1, "joe");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method5");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo(false);
	}

	@Test
	public void testResultNull() throws IOException {
		Map<String, Object> edRequest = ControllerUtil
				.createRequestJson("remoteProviderSimple", "method5", 1, "martin");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method5");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isNull();
	}

	@Test
	public void testIntParameterAndResult() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method6", 3, 10, 20);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		checkIntParameterResult(resp, 3, 30);
	}

	@Test
	public void testIntParameterAndResultWithTypeConversion() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method6", 3, "30",
				"40");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		checkIntParameterResult(resp, 3, 70);
	}

	static void checkIntParameterResult(ExtDirectResponse resp, int tid, int result) {
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method6");
		assertThat(resp.getTid()).isEqualTo(tid);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo(result);
	}

	@Test
	public void testResultStringNull() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method7", 1, null);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method7");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isNull();
	}

	@Test
	public void testReturnsObject() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method8", 1, 7.34);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);

		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method8");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isNotNull();

		FormInfo info = ControllerUtil.convertValue(resp.getResult(), FormInfo.class);

		assertThat(Double.compare(7.34, info.getBack()) == 0).isTrue();
		assertThat(info.isAdmin()).isEqualTo(false);
		assertThat(info.getAge()).isEqualTo(32);
		assertThat(info.getName()).isEqualTo("John");
		assertThat(info.getSalary()).isEqualTo(new BigDecimal("8720.2"));
		assertThat(info.getBirthday()).isEqualTo(new GregorianCalendar(1986, Calendar.JULY, 22).getTime());
	}

	@Test
	public void testSupportedArguments() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method9", 1, null);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);

		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method9");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo(42);
	}

	@Test
	public void testTypeConversion() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method10", 3, "true",
				"c", "ACTIVE", "14", "21", "3.14", "10.01", "1", "2");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method10");
		assertThat(resp.getTid()).isEqualTo(3);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo("method10() called-true-c-ACTIVE-14-21-3.14-10.01-1-2");

	}

	@Test
	public void testMixParameterAndSupportedParameters() throws IOException {
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method13", 4, "true",
				"c", "ACTIVE", "14", "21", "3.14", "10.01", "1", "2");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method13");
		assertThat(resp.getTid()).isEqualTo(4);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();
		assertThat(resp.getResult()).isEqualTo("method13() called-true-c-ACTIVE-14-21-3.14-10.01-1-2");
	}

	@Test
	public void testWithConversion() throws IOException {

		DateTime today = new DateTime();

		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method14", 1,
				ISODateTimeFormat.dateTime().print(today), "normalParameter", ISODateTimeFormat.date().print(today),
				"99.9%");

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method14");
		assertThat(resp.getTid()).isEqualTo(1);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();

		Map<String, Object> resultMap = (Map<String, Object>) resp.getResult();
		assertThat(resultMap.get("endDate")).isEqualTo(today.getMillis());
		ObjectMapper mapper = new ObjectMapper();

		List<Object> expectedValue = mapper.readValue(mapper.writeValueAsString(today.toLocalDate()), List.class);
		Object actualValue = resultMap.get("jodaLocalDate");

		assertThat((List<Object>) resultMap.get("jodaLocalDate")).isEqualTo(expectedValue);
		assertThat(resultMap.get("percent")).isEqualTo(0.999);
		assertThat(resultMap.get("normalParameter")).isEqualTo("normalParameter");
		assertThat(resultMap.get("remoteAddr")).isEqualTo("127.0.0.1");
	}

	@Test
	public void testTypeConversionWithObjects() throws IOException {
		Row aRow = new Row(104, "myRow", true, "100.45");
		Map<String, Object> edRequest = ControllerUtil.createRequestJson("remoteProviderSimple", "method12", 5, aRow);

		request.setContent(ControllerUtil.writeAsByte(edRequest));
		controller.router(request, response, Locale.ENGLISH);
		List<ExtDirectResponse> responses = ControllerUtil.readDirectResponses(response.getContentAsByteArray());

		assertThat(responses).hasSize(1);
		ExtDirectResponse resp = responses.get(0);
		assertThat(resp.getAction()).isEqualTo("remoteProviderSimple");
		assertThat(resp.getMethod()).isEqualTo("method12");
		assertThat(resp.getTid()).isEqualTo(5);
		assertThat(resp.getType()).isEqualTo("rpc");
		assertThat(resp.getWhere()).isNull();
		assertThat(resp.getMessage()).isNull();

		assertThat(resp.getResult()).isEqualTo("Row [id=104, name=myRow, admin=true, salary=100.45]");
	}

}
