package org.gethydrated.swarm.core.mapping;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapperTest {
	
	private Mapper mapper;
	
	@Before
	public void setup() {
		mapper = new Mapper("ctx");
	}
	
	@After
	public void teardown() {
		mapper = null;
	}

	@Test
	public void testExactServlet() {
		mapper.addMapping("/test", "testServlet");
		MappingInfo info = new MappingInfo();
		assertEmptyMapping(info);
		mapper.map("/ctx/", info);
		assertMappingNull(info);
		info.clear();
		mapper.map("/ctx/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "/test", null);
		info.clear();
		mapper.map("/ctx/test/test", info);
		assertMappingNull(info);
		assertTrue(mapper.containsMapping("/test"));
	}
	
	@Test
	public void testPathServletShort() {
		mapper.addMapping("/*", "testServlet");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx", info);
		assertMappingInfo(info, "testServlet", "/ctx", "", "/");
		info.clear();
		mapper.map("/ctx/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "", "/test");
		info.clear();
		mapper.map("/ctx/test/", info);
		assertMappingInfo(info, "testServlet", "/ctx", "", "/test/");
		info.clear();
		mapper.map("/ctx/test/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "", "/test/test");
		info.clear();
		mapper.map("/ctx/test/test/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "", "/test/test/test");
		info.clear();
		mapper.map("/ctx/test/test/", info);
		assertMappingInfo(info, "testServlet", "/ctx", "", "/test/test/");
		assertTrue(mapper.containsMapping("/*"));
	}
	
	@Test
	public void testPathServletLong() {
		mapper.addMapping("/test/*", "testServlet");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx", info);
		assertMappingRedirect(info, "/");
		info.clear();
		mapper.map("/ctx/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "/test", null);
		info.clear();
		mapper.map("/ctx/test/", info);
		assertMappingInfo(info, "testServlet", "/ctx", "/test", "/");
		info.clear();
		mapper.map("/ctx/test/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "/test", "/test");
		info.clear();
		mapper.map("/ctx/test/test/test", info);
		assertMappingInfo(info, "testServlet", "/ctx", "/test", "/test/test");
		info.clear();
		mapper.map("/ctx/test/test/", info);
		assertMappingInfo(info, "testServlet", "/ctx", "/test", "/test/");
		assertTrue(mapper.containsMapping("/test/*"));
	}
	
	@Test
	public void testRootServlet() {
		mapper.addMapping("", "rootServlet");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx/test", info);
		assertMappingNull(info);
		info.clear();
		mapper.map("/ctx/", info);
		assertMappingInfo(info, "rootServlet", "", "", "/");
		info.clear();
		mapper.map("/ctx", info);
		assertMappingInfo(info, "rootServlet", "", "", "/");
		info.clear();
		mapper.map("/ctx/test/test", info);
		assertMappingNull(info);
		assertTrue(mapper.containsMapping(""));
	}

	@Test
	public void testDefaultServlet() {
		mapper.addMapping("/", "defaultServlet");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx/", info);
		assertMappingInfo(info, "defaultServlet", "/ctx", "/", null);
		info.clear();
		mapper.map("/ctx", info);
		assertMappingRedirect(info, "/");
		info.clear();
		mapper.map("/ctx/test", info);
		assertMappingInfo(info, "defaultServlet", "/ctx", "/test", null);
		info.clear();
		mapper.map("/ctx/test/test", info);
		assertMappingInfo(info, "defaultServlet", "/ctx", "/test/test", null);
		info.clear();
		mapper.map("/ctx/test/", info);
		assertMappingInfo(info, "defaultServlet", "/ctx", "/test/", null);
		assertTrue(mapper.containsMapping("/"));
	}
	
	@Test
	public void testMETAINF() {
		mapper.addMapping("/", "default");
		mapper.addMapping("/META-INF/*", "metainf");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx/META-INF/", info);
		assertMappingNull(info);
	}
	
	@Test
	public void testWEBINF() {
		mapper.addMapping("/", "default");
		mapper.addMapping("/WEB-INF/*", "webinf");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx/WEB-INF/", info);
		assertMappingNull(info);
	}
	
	@Test
	public void testJSP() {
		mapper.addMapping("*.jsp", "jsp");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx/test.jsp", info);
		assertMappingInfo(info, "jsp", "/ctx", "/test.jsp", null);
		info.clear();
		mapper.map("/ctx/WEB-INF/jsp/welcome.jsp", info);
		assertEmptyMapping(info);
		info.clear();
		mapper.mapInternal("/ctx/WEB-INF/jsp/welcome.jsp", info);
		assertMappingInfo(info, "jsp", "/ctx", "/WEB-INF/jsp/welcome.jsp", null);
		info.clear();
	}
	
	@Test
	public void testRootJSP() {
		mapper.addMapping("/", "default");
		mapper.addMapping("*.jsp", "jsp");
		MappingInfo info = new MappingInfo();
		mapper.map("/ctx/test.jsp", info);
		assertMappingInfo(info, "jsp", "/ctx", "/test.jsp", null);
	}
	
	private void assertMapping(MappingInfo info, String servletName, String contextPath,
			String servletPath, String pathInfo, String redirect) {
		assertEquals(contextPath, info.contextPath);
		assertEquals(servletName, info.servletName);
		assertEquals(servletPath, info.servletPath);
		assertEquals(pathInfo, info.pathInfo);
		assertEquals(redirect, info.redirect);
	}
	
	private void assertMappingInfo(MappingInfo info, String servletName, String contextPath,
			String servletPath, String pathInfo) {
		assertMapping(info, servletName, contextPath, servletPath, pathInfo, null);
	}
	
	private void assertMappingNull(MappingInfo info) {
		assertEmptyMapping(info);
	}
	
	private void assertMappingRedirect(MappingInfo info, String redirect) {
		assertMapping(info, null, null, null, null, redirect);
	}
	
	private void assertEmptyMapping(MappingInfo info) {
		assertTrue(info.isEmpty());
	}
}
