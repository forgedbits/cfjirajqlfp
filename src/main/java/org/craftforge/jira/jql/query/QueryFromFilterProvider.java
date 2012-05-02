/*
 * Copyright 2012 Craftware Sp. z o.o..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.craftforge.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;

/**
 *
 * @author pbojko
 */
public class QueryFromFilterProvider implements QueryProvider {

	private SearchRequestService searchRequestService;

	private I18nHelper i18nHelper;

	public QueryFromFilterProvider(SearchRequestService searchRequestService, I18nHelper i18nHelper) {
		this.searchRequestService = searchRequestService;
		this.i18nHelper = i18nHelper;
	}

	@Override
	public Query provide(User user, String filter) throws PermissionException {
		return fetchFilter(user, filter).getQuery();
	}

	private SearchRequest fetchFilter(final User user, final String filter) throws PermissionException {
		SearchRequest request = findFilter(user, filter);
		if (request == null) {
			throw new PermissionException(i18nHelper.getText("query-from-filter-provider.bad.saved.filter", filter, userName(user)));
		}
		return request;
	}

	private String userName(final User user) {
		return user != null ? user.getName() : "";
	}

	private SearchRequest findFilter(final User user, final String filter) throws IllegalArgumentException {
		SearchRequest result = tryToFetchById(user, filter);
		if (result != null) {
			return result;
		}
		return tryToFetchByName(user, filter);
	}

	private SearchRequest tryToFetchById(User user, String filter) {
		if (!filter.matches("\\d+")) {
			return null;
		}
		return searchRequestService.getFilter(new JiraServiceContextImpl(user), Long.valueOf(filter));
	}

	private SearchRequest tryToFetchByName(final User user, final String filter) throws IllegalArgumentException {
		SharedEntitySearchResult<SearchRequest> searchResult =
				searchRequestService.search(new JiraServiceContextImpl(user), prepareSharedEntirySearchParameyer(filter), 0, 100);
		SearchRequestByNameConsumer consumer = new SearchRequestByNameConsumer(filter);
		searchResult.foreach(consumer);
		return consumer.getFound();
	}

	private SharedEntitySearchParameters prepareSharedEntirySearchParameyer(String filter) {
		return new SharedEntitySearchParametersBuilder().setName(filter).
				setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT).
				toSearchParameters();
	}

	private static class SearchRequestByNameConsumer implements Consumer<SearchRequest> {

		private String name;

		private SearchRequest found = null;

		public SearchRequestByNameConsumer(String name) {
			this.name = name;
		}

		public void consume(SearchRequest t) {
			if (t.getName().equalsIgnoreCase(name)) {
				found = t;
			}
		}

		public SearchRequest getFound() {
			return found;
		}
	}
}
