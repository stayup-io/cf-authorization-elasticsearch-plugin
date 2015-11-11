package io.logsearch.elasticsearch.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class CFAuhorizationActionFilter extends AbstractComponent implements ActionFilter {
	
	private static final String ADMIN_ORG_NAME = "system";
	protected final ESLogger log = Loggers.getLogger(this.getClass().getSimpleName());
	
	@Inject
    public CFAuhorizationActionFilter(Settings settings, RestController controller) {
		super(settings);
		controller.registerRelevantHeaders("X-Authorized-SpaceIds", "X-Authorized-Orgs");
    }
    
	@Override
	public int order() {
		return Integer.MIN_VALUE;
	}

	@Override
	public void apply(String action, ActionRequest request,	ActionListener listener, ActionFilterChain chain) {
        
		log.debug("request {} headers {}", action, request.getHeaders()); 
		
		if (request.hasHeader("X-Authorized-SpaceIds")) {
			if (request instanceof MultiSearchRequest) {
				String[] authorizedSpaceIds = ((String) request.getHeader("X-Authorized-SpaceIds")).split(",[ ]*");
				TermsQueryBuilder spaceFilter = new TermsQueryBuilder("@source.space.id", authorizedSpaceIds);
				for (SearchRequest search : ((MultiSearchRequest)request).requests()) {
					log.debug("Appending space filter: {} to search on indices: {}", spaceFilter, search.indices());
					search.extraSource(SearchSourceBuilder.searchSource().query(spaceFilter));
				}
			}
		}
		
		if (request.hasHeader("X-Authorized-Orgs")) {
			if (!isAdminUser(request)) {
				ArrayList<IndicesRequest> requestIndices = new ArrayList<IndicesRequest>();
				if (request instanceof MultiGetRequest) {
					requestIndices.addAll( ((MultiGetRequest) request).getItems());
				}
				if (request instanceof MultiSearchRequest) {
					requestIndices.addAll( ((MultiSearchRequest) request).requests());
				}
				
				for (IndicesRequest subRequest: requestIndices) {
					for (String index: subRequest.indices()) {
						if (isRestrictedIndex(index) ) {
							throw new RuntimeException("Only members of the system org are authorised to query index: " + index);
						}
					}
				}
			}
		}
		
		chain.proceed(action, request, listener);
		
	}

	private Boolean isRestrictedIndex(String index) {
		return !index.startsWith(".kibana") && !index.startsWith("logs-app");
	}

	private Boolean isAdminUser(ActionRequest request) {
		String[] authorizedOrgNames = ((String) request.getHeader("X-Authorized-Orgs")).split(",[ ]*");
		Boolean isAdminUser = Arrays.asList(authorizedOrgNames).contains(ADMIN_ORG_NAME);
		return isAdminUser;
	}

	@Override
	public void apply(String action, ActionResponse response, ActionListener listener, ActionFilterChain chain) {
		
		chain.proceed(action, response, listener);
		
	}

}
