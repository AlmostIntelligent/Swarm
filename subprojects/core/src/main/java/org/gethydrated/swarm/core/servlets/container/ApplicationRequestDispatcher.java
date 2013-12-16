package org.gethydrated.swarm.core.servlets.container;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ApplicationRequestDispatcher implements RequestDispatcher {

	@Override
	public void forward(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		System.out.println("forward");
		
	}

	@Override
	public void include(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		System.out.println("include");
	}

}
