package com.axway.apim;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import com.vordel.controller.apimgr.client.ApiManagerApiClient;
import com.vordel.controller.apimgr.client.ApiManagerClientSession;

public class APIExportClient extends ApiManagerApiClient{
	

	private final WebTarget target;

	public APIExportClient(ApiManagerClientSession session) {
		super(session);
		this.target = getRootResource().path("/proxies");
	}

	public WebTarget getTargetResource() {
		return target;
	}
	
	
	public String exportVirtualizedAPI(List<String> ids,String filename)  {
		Invocation.Builder builder = getTargetResource().path("/export").request();
		Form formData = new Form() ;
		
		for (String id : ids) {
			formData.param("id", id);
		}
		
		formData.param("filename", filename);
		Entity<Form> entity=Entity.form(formData);
		Response response = builder.post(entity);
		
		//String pathname;
		//APIPromotion apiPromotion = null;
		//Entity<APIPromotion> entity1=Entity.entity(apiPromotion, MediaType.MULTIPART_FORM_DATA);
		int status = response.getStatus();

		if (status >= 400) {
			throw new WebApplicationException(response);
		}
		
		String locationResponse = response.getHeaderString("Location");
		String downloadUriFull=locationResponse.replace(target.getUri().toString(), "");
		downloadUriFull.substring(0,downloadUriFull.indexOf('?'));
		String downloadUri=downloadUriFull.substring(0,downloadUriFull.indexOf('?'));
			
		builder = getTargetResource().path(downloadUri).queryParam("filename", filename).request();
		response = builder.get();
		
		status = response.getStatus();
		if (status >= 400) {
			throw new WebApplicationException(response);
		}
		
		return response.readEntity(String.class);
	}

}
