package io.logsearch.elasticsearch.plugin;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CFAuhorizationActionFilterTest {
	
	@Mock
	private ActionListener<?> actionListener;
	
	@Mock
	private ActionFilterChain actionFilterChain;
	
	private CFAuhorizationActionFilter unit;
	
	@Before
	public void before() {
		Settings settings = Settings.builder().build();
		unit = new CFAuhorizationActionFilter(settings, new RestController(settings));
	}
	
	@Test
	public void testRequestIsNotModifiedWhenThereAreNoAuthorizationHeaders() {

		ActionRequest<MultiSearchRequest> actionRequest = Mockito.mock(MultiSearchRequest.class);
		
		Mockito.when(actionRequest.hasHeader("X-Authorized-Orgs")).thenReturn(false);
		
		unit.apply("", actionRequest, actionListener, actionFilterChain);
			
	}
}
