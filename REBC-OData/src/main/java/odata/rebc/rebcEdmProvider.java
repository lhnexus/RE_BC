package odata.rebc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

public class rebcEdmProvider extends CsdlAbstractEdmProvider {

	private static final Logger LOG = LoggerFactory
			.getLogger(rebcEdmProvider.class);

	// Service Namespace
	public static final String NAMESPACE = "OData.rebc";

	// EDM Container
	public static final String CONTAINER_NAME = "Container";
	public static final FullQualifiedName CONTAINER = new FullQualifiedName(
			NAMESPACE, CONTAINER_NAME);

	// Entity Types Names
	public static final String ET_HOUSE = "House";
	public static final FullQualifiedName ET_HOUSE_FQN = new FullQualifiedName(
			NAMESPACE, ET_HOUSE);

	public static final String ET_LEASE = "Lease";
	public static final FullQualifiedName ET_LEASE_FQN = new FullQualifiedName(
			NAMESPACE, ET_LEASE);

	// Entity Set Names
	public static final String ES_HOUSES = "Houses";
	public static final String ES_LEASES = "Leases";

	
	// Action
	public static final String ACTION_LOAD = "Load";
	public static final FullQualifiedName ACTION_LOAD_FQN = new FullQualifiedName(NAMESPACE, ACTION_LOAD);

	public static final String PARAMETER_PROJECT = "project";
	public static final String PARAMETER_OWNER = "owner";
	
	
	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {

		LOG.debug("getEntityContainer()");

		// create EntitySets
		List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
		entitySets.add(getEntitySet(CONTAINER, ES_HOUSES));
		entitySets.add(getEntitySet(CONTAINER, ES_LEASES));

		// create EntityContainer
		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);
		
		// Create action imports
		List<CsdlActionImport> actionImports = new ArrayList<CsdlActionImport>();
		actionImports.add(getActionImport(CONTAINER, ACTION_LOAD));
 
		entityContainer.setActionImports(actionImports);

		return entityContainer;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(
			FullQualifiedName entityContainerName) throws ODataException {

		LOG.debug("getEntityContainerInfo()");

		// This method is invoked when displaying the Service Document at e.g.
		// http://localhost:8080/DemoService/DemoService.svc
		if (entityContainerName == null
				|| entityContainerName.equals(CONTAINER)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER);
			return entityContainerInfo;
		}

		return null;
	}

	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer,
			String entitySetName) throws ODataException {

		LOG.debug("getEntitySet()");

		CsdlEntitySet entitySet = null;

		if (entityContainer.equals(CONTAINER)) {

			if (entitySetName.equals(ES_HOUSES)) {
				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_HOUSES);
				entitySet.setType(ET_HOUSE_FQN);

				CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
				navPropBinding.setPath(ES_LEASES); // the path from entity type
													// to navigation property
				navPropBinding.setTarget(ES_LEASES); // target entitySet,
														// where the nav prop
														// points to
				List<CsdlNavigationPropertyBinding> navPropBindingList = 
						new ArrayList<CsdlNavigationPropertyBinding>();
				navPropBindingList.add(navPropBinding);
				entitySet.setNavigationPropertyBindings(navPropBindingList);

			} else if (entitySetName.equals(ES_LEASES)) {

				entitySet = new CsdlEntitySet();
				entitySet.setName(ES_LEASES);
				entitySet.setType(ET_LEASE_FQN);

				// navigation
				CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
				navPropBinding.setTarget(ES_HOUSES); // the target entity set,
														// where the navigation
														// property points to
				navPropBinding.setPath(ET_HOUSE); // the path from entity type
													// to navigation property 
				
				List<CsdlNavigationPropertyBinding> navPropBindingList = 
						new ArrayList<CsdlNavigationPropertyBinding>();
				navPropBindingList.add(navPropBinding);
				entitySet.setNavigationPropertyBindings(navPropBindingList);
			}
		}

		return entitySet;
	}

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName)
			throws ODataException {

		LOG.debug("getEntityType()");

		// this method is called for each EntityType that are configured in the
		// Schema
		CsdlEntityType entityType = null;

		// this method is called for one of the EntityTypes that are configured
		// in the Schema
		if (entityTypeName.equals(ET_HOUSE_FQN)) {

			// create EntityType properties
			CsdlProperty UUID = new CsdlProperty().setName("UUID").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty PRO_NAME = new CsdlProperty()
					.setName("PRO_NAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty ADDRESS = new CsdlProperty()
					.setName("ADDRESS")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty OWNER = new CsdlProperty().setName("OWNER").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty UPDATER = new CsdlProperty().setName("UPDATER").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty STATUS = new CsdlProperty().setName("STATUS").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create CsdlPropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("UUID");

			// navigation property: one-to-many, null not allowed (product must
			// have a category)
			CsdlNavigationProperty navProp = new CsdlNavigationProperty()
					.setName("Leases").setType(ET_LEASE_FQN)
					.setCollection(true).setPartner("House");
			List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
			navPropList.add(navProp);
//
			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_HOUSE);
			entityType.setProperties(Arrays.asList(UUID, PRO_NAME, ADDRESS,
					OWNER, UPDATER, STATUS));
			entityType.setKey(Collections.singletonList(propertyRef));
			entityType.setNavigationProperties(navPropList);
			 

		} else if (entityTypeName.equals(ET_LEASE_FQN)) {
			// create EntityType properties
			CsdlProperty id = new CsdlProperty().setName("ID").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty Timestamp = new CsdlProperty()
					.setName("Timestamp")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty UUID = new CsdlProperty().setName("UUID").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty PRO_NAME = new CsdlProperty()
					.setName("PRO_NAME")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty ADDRESS = new CsdlProperty()
					.setName("ADDRESS")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty PRICE = new CsdlProperty().setName("PRICE").setType(
					EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
			CsdlProperty LEASE_FROM = new CsdlProperty().setName("LEASE_FROM")
					.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
			CsdlProperty LEASE_TO = new CsdlProperty().setName("LEASE_TO")
					.setType(EdmPrimitiveTypeKind.Date.getFullQualifiedName());
			CsdlProperty OWNER = new CsdlProperty().setName("OWNER").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty TENANT = new CsdlProperty().setName("TENANT").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty APPLIER = new CsdlProperty()
					.setName("APPLIER")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty TERMS = new CsdlProperty().setName("TERMS").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty STATUS = new CsdlProperty().setName("STATUS").setType(
					EdmPrimitiveTypeKind.String.getFullQualifiedName());
			CsdlProperty UPDATER = new CsdlProperty()
					.setName("UPDATER")
					.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

			// create PropertyRef for Key element
			CsdlPropertyRef propertyRef = new CsdlPropertyRef();
			propertyRef.setName("ID");

			// navigation property: many-to-one
			CsdlNavigationProperty navProp = new CsdlNavigationProperty()
					.setName("House").setType(ET_HOUSE_FQN).setNullable(false)
					.setPartner("Leases");
			List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
			navPropList.add(navProp);

//			// configure EntityType
			entityType = new CsdlEntityType();
			entityType.setName(ET_LEASE);
			entityType.setProperties(Arrays.asList(id, Timestamp, UUID,
					PRO_NAME, ADDRESS, PRICE, LEASE_FROM, LEASE_TO, OWNER,
					TENANT, APPLIER, TERMS, STATUS, UPDATER)); 
			entityType.setKey(Collections.singletonList(propertyRef));
			entityType.setNavigationProperties(navPropList);
		}

		return entityType;
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {

		LOG.debug("getSchemas()");

		// create Schema
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAMESPACE);

		// add EntityTypes
		List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		entityTypes.add(getEntityType(ET_HOUSE_FQN));
		entityTypes.add(getEntityType(ET_LEASE_FQN));
		schema.setEntityTypes(entityTypes);

		// add EntityContainer
		schema.setEntityContainer(getEntityContainer());
		
		// add actions
		List<CsdlAction> actions = new ArrayList<CsdlAction>();
		actions.addAll(getActions(ACTION_LOAD_FQN));
		schema.setActions(actions);
		

		// finally
		List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		schemas.add(schema);

		return schemas;
	}
	
	@Override
	public List<CsdlAction> getActions(final FullQualifiedName actionName) {
	  if(actionName.equals(ACTION_LOAD_FQN)) {
	    // It is allowed to overload actions, so we have to provide a list of Actions for each action name
	    final List<CsdlAction> actions = new ArrayList<CsdlAction>();

	    // Create parameters
	    final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
	    final CsdlParameter parameter = new CsdlParameter();
	    parameter.setName(PARAMETER_PROJECT); 
	    parameter.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
	    parameters.add(parameter);

	    final CsdlParameter parameter2 = new CsdlParameter();
	    parameter2.setName(PARAMETER_OWNER); 
	    parameter2.setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
	    parameters.add(parameter2);
	    
	    // Create the Csdl Action
	    final CsdlAction action = new CsdlAction();
	    action.setName(ACTION_LOAD_FQN.getName());
	    action.setParameters(parameters);
	    actions.add(action);

	    return actions;
	  }

	  return null;
	}

	@Override
	public CsdlActionImport getActionImport(final FullQualifiedName entityContainer, final String actionImportName) {
	  if(entityContainer.equals(CONTAINER)) {
	    if(actionImportName.equals(ACTION_LOAD_FQN.getName())) {
	      return new CsdlActionImport()
	              .setName(actionImportName)
	              .setAction(ACTION_LOAD_FQN);
	    }
	  }

	  return null;
	}
}
