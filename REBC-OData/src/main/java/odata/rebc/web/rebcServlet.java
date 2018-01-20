package odata.rebc.web;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 

import odata.rebc.rebcActionProcessor;
import odata.rebc.rebcEdmProvider;
import odata.rebc.rebcEntityCollectionProcessor;
import odata.rebc.rebcEntityProcessor;
import odata.rebc.rebcPrimitiveProcessor;
import odata.rebc.model.OModel;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class rebcServlet extends HttpServlet{

//	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(rebcServlet.class);
	
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
	    try {
	    	HttpSession session = req.getSession(true);
	    	OModel storage = (OModel) session.getAttribute(OModel.class.getName());
	        if (storage == null) {
	           storage = new OModel();
	           session.setAttribute(OModel.class.getName(), storage);
	        }

	        // create odata handler and configure it with EdmProvider and Processor
	        OData odata = OData.newInstance();
	        ServiceMetadata edm = odata.createServiceMetadata(new rebcEdmProvider(), new ArrayList<EdmxReference>());
	        ODataHttpHandler handler = odata.createHandler(edm);
	        handler.register(new rebcEntityCollectionProcessor(storage));
	        handler.register(new rebcEntityProcessor(storage));
	        handler.register(new rebcPrimitiveProcessor(storage));
	        handler.register(new rebcActionProcessor(storage));
	        
	        // let the handler do the work
	        handler.process(req, resp);
	    } catch (RuntimeException e) {
	      LOG.error("Server Error occurred in REBC Servlet", e);
	      throw new ServletException(e);
	    }
	  }
}
