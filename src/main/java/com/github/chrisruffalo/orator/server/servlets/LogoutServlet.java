package com.github.chrisruffalo.orator.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(
	name = "logout",
	urlPatterns = {
		"/logout"
	}
)
public class LogoutServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// use built-in logout feature
		req.logout();
		
		// invalidate session
		req.getSession().invalidate();
		
		// redirect back to login
		resp.sendRedirect("./index.jsp");
	}
	
}
