package odata.rebc.model;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import odata.olingoV4.DemoEdmProvider;
import odata.rebc.rebcEdmProvider;
import odata.rebc.util.REBCUtil;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OModel {

	private static final Logger LOG = LoggerFactory.getLogger(OModel.class);


	public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) {
		// TODO Auto-generated method stub

		LOG.info("readEntityData:");

		Entity entity = null;

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		if (edmEntityType.getName().equals(rebcEdmProvider.ET_LEASE)) {
			// entity = getProduct(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(rebcEdmProvider.ET_HOUSE)) {
			UriParameter key = keyPredicates.get(0);
			if(keyPredicates.size()>0){
				String keyname = key.getName();
				String keytext = key.getText().replace("'", "");
				entity = getHouse(keytext);
			}
			
		}

		return entity;

	}

	public EntityCollection processBoundActionEntityCollection(EdmAction action, Map<String, Parameter> parameters) {
		EntityCollection collection = new EntityCollection();
		// if (ACTION_PROVIDE_DISCOUNT.equals(action.getName())) {
		// for (Entity entity : categoryList) {
		// Entity en = getRelatedEntity(entity, (EdmEntityType)
		// action.getReturnType().getType());
		// Integer currentValue =
		// (Integer)en.getProperty(PRICE_PROPERTY).asPrimitive();
		// Integer newValue = currentValue -
		// (Integer)parameters.get(AMOUNT_PROPERTY).asPrimitive();
		// en.getProperty(PRICE_PROPERTY).setValue(ValueType.PRIMITIVE,
		// newValue);
		// collection.getEntities().add(en);
		// }
		// }
		return collection;
	}

	public rebcEntityActionResult processBoundActionEntity(EdmAction action, Map<String, Parameter> parameters,
			List<UriParameter> keyPredicates) {
		rebcEntityActionResult result = new rebcEntityActionResult();
		// if (ACTION_PROVIDE_DISCOUNT_FOR_PRODUCT.equals(action.getName())) {
		// for (Entity entity : categoryList) {
		// Entity en = getRelatedEntity(entity, (EdmEntityType)
		// action.getReturnType().getType(), keyParams);
		// Integer currentValue =
		// (Integer)en.getProperty(PRICE_PROPERTY).asPrimitive();
		// Integer newValue = currentValue -
		// (Integer)parameters.get(AMOUNT_PROPERTY).asPrimitive();
		// en.getProperty(PRICE_PROPERTY).setValue(ValueType.PRIMITIVE,
		// newValue);
		// result.setEntity(en);
		// result.setCreated(true);
		// return result;
		// }
		// }
		return null;
	}

	public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet, List<CustomQueryOption> list) {
		EntityCollection entitySet = null;

		if (edmEntitySet.getName().equals(rebcEdmProvider.ES_LEASES)) {
			String status = null;
			for (CustomQueryOption option : list) {
				if (option.getName().equals("status")) {
					status = option.getText();
					break;
				}
			}
			for (CustomQueryOption option : list) {
				if (option.getName().equals("PRO_NAME") && !option.getText().isEmpty()) {
					// Search project
					entitySet = getProjectHouses(option.getText(), status);
					break;
				}

				if (option.getName().equals("OWNER") && !option.getText().isEmpty()) {
					// Search owned houses
					entitySet = getOwnedHouses(option.getText(), status);
					break;
				}

				if (option.getName().equals("TENANT") && !option.getText().isEmpty()) {
					// Search project
					entitySet = getRentedHouses(option.getText(), status);
					break;
				}

				if (option.getName().equals("APPLIER") && !option.getText().isEmpty()) {
					// Search project
					entitySet = getAppliedHouses(option.getText(), status);
					break;
				}

			}
			// entitySet = getProducts();
		} else if (edmEntitySet.getName().equals(rebcEdmProvider.ES_HOUSES)) {
			// entitySet = getCategories();
		}

		return entitySet;
	}

	private EntityCollection getProjectHouses(String project, String status) {

		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		req.fcn = "searchbyname";

		req.args.add(project);
		req.args.add("contain");
		if (!status.isEmpty()) {
			req.args.add("Status");
		}

		EntityCollection ec = new EntityCollection();

		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);

		if (response != null) {

			try {
				String source = formatJson(response);
				EntitySetResp resp = mapper.readValue(source, EntitySetResp.class);
				
				

				for (int i = 0; i < resp.data.size(); i++) {
					HouseLease hl = resp.data.get(i);
					Entity entity = new Entity();
					entity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, hl.getUUID()));
					entity.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, hl.getPRO_NAME()));
					entity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, hl.getADDRESS()));
					entity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, hl.getOWNER()));
					entity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, hl.getSTATUS()));

					entity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
					entity.setId(createId(entity, "UUID"));
					ec.getEntities().add(entity);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ec;
	}

	private EntityCollection getAppliedHouses(String applier, String status) {

		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		req.fcn = "searchbyapplier";

		req.args.add(applier);
		req.args.add("equal");
		if (!status.isEmpty()) {
			req.args.add("Status");
		}

		EntityCollection ec = new EntityCollection();

		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);

		if (response != null) {

			try {
				String source = formatJson(response);
				EntitySetResp resp = mapper.readValue(source, EntitySetResp.class);
				
				
				for (int i = 0; i < resp.data.size(); i++) {
					HouseLease hl = resp.data.get(i);
					Entity entity = new Entity();
					entity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, hl.getUUID()));
					entity.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, hl.getPRO_NAME()));
					entity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, hl.getADDRESS()));
					entity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, hl.getOWNER()));
					entity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, hl.getSTATUS()));

					entity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
					entity.setId(createId(entity, "UUID"));
					ec.getEntities().add(entity);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ec;
	}

	public EntityCollection getRelatedEntityCollection(Entity sourceEntity, EdmEntityType targetEntityType) throws JsonParseException, JsonMappingException, IOException {
		EntityCollection navigationTargetEntityCollection = new EntityCollection();

		FullQualifiedName relatedEntityFqn = targetEntityType.getFullQualifiedName();
		String sourceEntityFqn = sourceEntity.getType();

		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		ObjectMapper mapper = new ObjectMapper();

		if (sourceEntityFqn.equals(rebcEdmProvider.ET_LEASE_FQN.getFullQualifiedNameAsString())
				&& relatedEntityFqn.equals(rebcEdmProvider.ET_HOUSE_FQN)) {
			// relation Lease - > House
			String UUID = sourceEntity.getProperty("UUID").getValue().toString();

			req.fcn = "read";
			req.args.add(UUID);
			req.args.add("equal");

			try {
				json = mapper.writeValueAsString(req);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);
			EntitySetResp resp = null;
			String source = formatJson(response);

			try {
				
				resp = mapper.readValue(source, EntitySetResp.class);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Entity houseEntity = new Entity();
			
			
			houseEntity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, resp.data.get(0).getUUID()));
			houseEntity
					.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, resp.data.get(0).getPRO_NAME()));
			houseEntity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, resp.data.get(0).getADDRESS()));
			houseEntity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, resp.data.get(0).getOWNER()));
			houseEntity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, resp.data.get(0).getSTATUS()));

			houseEntity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
			houseEntity.setId(createId(houseEntity, "UUID"));
			navigationTargetEntityCollection.getEntities().add(houseEntity);

		} else if (sourceEntityFqn.equals(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString())
				&& relatedEntityFqn.equals(rebcEdmProvider.ET_LEASE_FQN)) {
			// relation House -> Lease
			String UUID = sourceEntity.getProperty("UUID").getValue().toString();

			req.fcn = "searchbyhistory";
			req.args.add(UUID);

			try {
				json = mapper.writeValueAsString(req);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);
			TransactionListResp resp = null;
			try {
				resp = mapper.readValue(response, TransactionListResp.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (Transaction transaction : resp.data) {
				Entity leaseEntity = new Entity();

				leaseEntity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, transaction.ID));
				leaseEntity.addProperty(new Property(null, "Timestamp", ValueType.PRIMITIVE, transaction.Timestamp));

				leaseEntity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, transaction.Value.getUUID()));
				leaseEntity.addProperty(
						new Property(null, "PRO_NAME", ValueType.PRIMITIVE, transaction.Value.getPRO_NAME()));
				leaseEntity.addProperty(
						new Property(null, "ADDRESS", ValueType.PRIMITIVE, transaction.Value.getADDRESS()));
				leaseEntity.addProperty(new Property(null, "PRICE", ValueType.PRIMITIVE, transaction.Value.getPRICE()));
				leaseEntity.addProperty(
						new Property(null, "LEASE_FROM", ValueType.PRIMITIVE, transaction.Value.getLEASE_FROM()));
				leaseEntity.addProperty(
						new Property(null, "LEASE_TO", ValueType.PRIMITIVE, transaction.Value.getLEASE_TO()));
				leaseEntity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, transaction.Value.getOWNER()));
				leaseEntity
						.addProperty(new Property(null, "TENANT", ValueType.PRIMITIVE, transaction.Value.getTENANT()));
				leaseEntity.addProperty(
						new Property(null, "APPLIER", ValueType.PRIMITIVE, transaction.Value.getAPPLIER()));
				leaseEntity.addProperty(new Property(null, "TERMS", ValueType.PRIMITIVE, transaction.Value.getTERMS()));
				leaseEntity
						.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, transaction.Value.getSTATUS()));
				leaseEntity.addProperty(
						new Property(null, "UPDATER", ValueType.PRIMITIVE, transaction.Value.getUPDATER()));

				leaseEntity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
				leaseEntity.setId(createId(leaseEntity, "ID"));

				navigationTargetEntityCollection.getEntities().add(leaseEntity);
			}

		}

		if (navigationTargetEntityCollection.getEntities().isEmpty()) {
			return null;
		}

		return navigationTargetEntityCollection;
	}

	public Entity createEntityData(EdmEntitySet edmEntitySet, Entity requestEntity) {
		// TODO Auto-generated method stub
		LOG.debug("createEntityData:");

		Entity entity = null;

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		if (edmEntityType.getName().equals(rebcEdmProvider.ET_LEASE)) {
			// entity = getProduct(edmEntityType, keyParams);
		} else if (edmEntityType.getName().equals(rebcEdmProvider.ET_HOUSE)) {
			return createHouse(edmEntityType, requestEntity);
		
			
		}

		return entity;
	}

	public void updateEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, Entity requestEntity,
			HttpMethod httpMethod) {
		// TODO Auto-generated method stub

	}

	public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) {
		// TODO Auto-generated method stub

	}

	public boolean loadREHouses(String project, String owner) {
		ArrayList<REHouse> reHouses = REBCUtil.readREHouseWithProject(project);
		LOG.info("Number of house loaded: " + reHouses.size());
		for (int i = 0; i < reHouses.size(); i++) {
			String json = null;
			REHouse house = reHouses.get(i);

			BRequestObj req = new BRequestObj();
			req.chaincodeId = REBCUtil.chainCodeID;
			req.fcn = "register";

			req.args.add(mashalREName(house.Aoid));
			req.args.add(mashalREName(house.XmName));
			req.args.add(mashalREName(house.FyName));
			req.args.add(owner);
			req.args.add(owner);
			ObjectMapper mapper = new ObjectMapper();
			try {
				json = mapper.writeValueAsString(req);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			 REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);
			

		}

		return true;
	}

	public EntityCollection getOwnedHouses(String owner, String status) {

		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		req.fcn = "search_owner";

		req.args.add(owner);
		req.args.add("equal");
		if (!status.isEmpty()) {
			req.args.add("Status");
		}

		EntityCollection ec = new EntityCollection();

		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);

		if (response != null) {

			try {
				String source = formatJson(response);
				EntitySetResp resp = mapper.readValue(source, EntitySetResp.class);
				
				
				for (int i = 0; i < resp.data.size(); i++) {
					HouseLease hl = resp.data.get(i);
					Entity entity = new Entity();
					entity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, hl.getUUID()));
					entity.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, hl.getPRO_NAME()));
					entity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, hl.getADDRESS()));
					entity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, hl.getOWNER()));
					entity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, hl.getSTATUS()));

					entity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
					entity.setId(createId(entity, "UUID"));
					ec.getEntities().add(entity);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ec;

	}

	public EntityCollection getRentedHouses(String tenant, String status) {

		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		req.fcn = "search_tenant";

		req.args.add(tenant);
		req.args.add("equal");
		if (!status.isEmpty()) {
			req.args.add("Status");
		}

		EntityCollection ec = new EntityCollection();

		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);

		if (response != null) {

			try {
				String source = formatJson(response);
				EntitySetResp resp = mapper.readValue(source, EntitySetResp.class);
				
//				ArrayList<HouseLease> respdata = mapper.readValue(resp.data, ArrayList.class);
				for (int i = 0; i < resp.data.size(); i++) {
					HouseLease hl = resp.data.get(i);
					Entity entity = new Entity();
					entity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, hl.getUUID()));
					entity.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, hl.getPRO_NAME()));
					entity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, hl.getADDRESS()));
					entity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, hl.getOWNER()));
					entity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, hl.getSTATUS()));

					entity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
					entity.setId(createId(entity, "UUID"));
					ec.getEntities().add(entity);
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ec;

	}

	private URI createId(Entity entity, String idPropertyName) {
		return createId(entity, idPropertyName, null);
	}

	private URI createId(Entity entity, String idPropertyName, String navigationName) {
		try {
			StringBuilder sb = new StringBuilder(getEntitySetName(entity)).append("(");
			final Property property = entity.getProperty(idPropertyName);
			sb.append(property.asPrimitive()).append(")");
			if (navigationName != null) {
				sb.append("/").append(navigationName);
			}
			return new URI(sb.toString());
		} catch (URISyntaxException e) {
			throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
		}
	}

	private String getEntitySetName(Entity entity) {
		if (rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return rebcEdmProvider.ES_HOUSES;
		} else if (rebcEdmProvider.ET_LEASE_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
			return rebcEdmProvider.ES_LEASES;
		}
		return entity.getType();
	}

	public Entity getHouse(String uuid) {

		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		req.fcn = "read";

		req.args.add(uuid);
		req.args.add("equal");

		Entity entity = new Entity();

		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.info("getHouse json: " + json);

		String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);
		
		LOG.info("getHouse: " + response);

		if (response != null) {

			try {
				String source = formatJson(response);
				
				
			
				EntitySetResp resp = mapper.readValue(source, EntitySetResp.class);	
	
				HouseLease hl = resp.data.get(0);
				


				entity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, hl.getUUID()));
				entity.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, hl.getPRO_NAME()));
				entity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, hl.getADDRESS()));
				entity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, hl.getOWNER()));
				entity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, hl.getSTATUS()));

				entity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
				entity.setId(createId(entity, "UUID"));

			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return entity;

	}
	
	private Entity createHouse(EdmEntityType edmEntityType, Entity entity) {

		// the ID of the newly created product entity is generated automatically
		
		String json = null;
		BRequestObj req = new BRequestObj();
		req.chaincodeId = REBCUtil.chainCodeID;
		req.fcn = "register";
		
		//LOG.info("createHouse entity="+entity.toString());
		Property uuidProperty = entity.getProperty("UUID");
		Property pronameProperty = entity.getProperty("PRO_NAME");
		Property addressProperty = entity.getProperty("ADDRESS");
		Property ownerProperty = entity.getProperty("OWNER");
		Property updaterProperty = entity.getProperty("UPDATER");
		
		
		
		req.args.add(uuidProperty.getValue().toString());
		req.args.add(pronameProperty.getValue().toString());
		req.args.add(addressProperty.getValue().toString());
		req.args.add(ownerProperty.getValue().toString());
		req.args.add(updaterProperty.getValue().toString());
	
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(req);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	

		String response = REBCUtil.httpPostIgnoreSSL(REBCUtil.BCUrl, json);
		

		if (response != null) {

			try {
				LOG.info("createHouse response="+response);
				String source = formatSimpleJson(response);
				LOG.info("createHouse source="+source);
				 
			
				EntityResp resp = mapper.readValue(source, EntityResp.class);	
	
				HouseLease hl = resp.data;			


				entity.addProperty(new Property(null, "UUID", ValueType.PRIMITIVE, hl.getUUID()));
				entity.addProperty(new Property(null, "PRO_NAME", ValueType.PRIMITIVE, hl.getPRO_NAME()));
				entity.addProperty(new Property(null, "ADDRESS", ValueType.PRIMITIVE, hl.getADDRESS()));
				entity.addProperty(new Property(null, "OWNER", ValueType.PRIMITIVE, hl.getOWNER()));
				entity.addProperty(new Property(null, "STATUS", ValueType.PRIMITIVE, hl.getSTATUS()));

				entity.setType(rebcEdmProvider.ET_HOUSE_FQN.getFullQualifiedNameAsString());
				entity.setId(createId(entity, "UUID"));

			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return entity;
	}
	
	private String formatSimpleJson(String input){
		String source = input.replace("\\", "");
		String tmp = source.substring(source.indexOf("data")+7,source.length()-2);
		String prefix = source.substring(0, source.indexOf("data")+6);
		String output = prefix+tmp+"}";		
		return output;
	}
	
	private String formatJson(String input){
		String source = input.replace("\\", "");
		String tmp = source.substring(source.indexOf("["),source.indexOf("]")+1);
		String prefix = source.substring(0, source.indexOf("[")-1);
		String output = prefix+tmp+"}";		
		return output;
	}
	
	private String mashalREName(String input){
		return input.replace("/", "");   
	}
}
