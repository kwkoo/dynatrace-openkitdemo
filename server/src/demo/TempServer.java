package demo;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TempServer extends HttpServlet
{
	Recorder rec;

	public void init() throws ServletException
	{
		rec = new SimpleRecorder();
		rec.init();
	}

	public void service(HttpServletRequest	request,
						HttpServletResponse	response)
		throws ServletException, IOException
	{
		String name = request.getParameter("name");
		if (name != null)
		{
			registerName(name);
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html>");
			out.println("<head><title>Hello World</title></head>");
			out.println("<body>");
			out.println("<h1>Hello World</h1>");
			out.println("<h3>Hello " + name + "!</h3>");
			out.println("</body>");
			out.println("</html>");
			return;
		}

		// Set response content type
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		String tempString = null;
		String pathInfo = request.getPathInfo();
		if ((pathInfo != null) && (pathInfo.length() > 1))
			tempString = pathInfo.substring(1);

		if (tempString == null) tempString = request.getParameter("temp");

		if (tempString == null)
		{
			out.println("temp parameter not found");
		}
		else
		{
			try
			{
				float temp = Float.parseFloat(tempString);
				rec.record(temp);
				out.println("recorded temperature reading of " + temp);
			}
			catch (NumberFormatException e)
			{
				out.println(tempString + " is not a float");
			}
		}

		out.println("reading count = " + rec.getReadingCount());
	}

	private void registerName(String n)
	{
		try { Thread.sleep(500); } catch (InterruptedException e) {}
	}
}
