/*
 * Copyright 2010 Craftware Sp. z o.o..
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
 * under the License.
 */
package org.craftforge.jira.jql;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.opensymphony.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author pbojko
 */
public abstract class AbstractIssuesFromFilterFunction extends AbstractJqlFunction {

    protected SearchRequestService searchRequestService;
    protected SearchProvider searchProvider;
    protected I18nHelper i18nHelper;

    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public void init(JqlFunctionModuleDescriptor moduleDescriptor) {
        super.init(moduleDescriptor);
        ComponentManager componentManager = ComponentManager.getInstance();
        searchRequestService = componentManager.getSearchRequestService();
        searchProvider = componentManager.getSearchProvider();
        i18nHelper = moduleDescriptor.getI18nBean();
    }

    @Override
    public boolean isList() {
        return true;
    }

    public MessageSet validate(User user, FunctionOperand operand, TerminalClause tc) {
        MessageSet messages = new MessageSetImpl();
        final List<String> args = operand.getArgs();
        if (args.isEmpty()) {
            messages.addErrorMessage(i18nHelper.getText("abstract-issues-from-filter.bad.num.arguments", operand.getName()));
        }
        if (savedFilterIsInvalid(user, args.get(0))) {
            messages.addErrorMessage(i18nHelper.getText("abstract-issues-from-filter.bad.saved.filter", args.get(0)));
        }
        return messages;
    }

    private SearchRequest fetchFilter(final User user, final String filter) {
        SearchRequest result = tryToFetchById(user,filter);
        if (result != null)
            return result;
        return tryToFetchByName(user, filter);
    }

    private SearchRequest tryToFetchByName(final User user, final String filter) throws IllegalArgumentException {
        SharedEntitySearchResult<SearchRequest> searchResult = searchRequestService.search(new JiraServiceContextImpl(user), prepareSharedEntirySearchParameyer(filter), 0, 100);
        SearchRequestByNameConsumer consumer = new SearchRequestByNameConsumer(filter);
        searchResult.foreach(consumer);
        return consumer.getFound();
    }

    protected List<Issue> fetchIssuesFromSubfilter(QueryCreationContext qcc, FunctionOperand fo) {
        try {
            SearchRequest request = fetchFilter(qcc.getUser(), fo.getArgs().get(0));
            List<Issue> issues = searchProvider.search(request.getQuery(), qcc.getUser(), new PagerFilter()).getIssues();
            return issues;
        } catch (SearchException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected boolean savedFilterIsInvalid(final User user, final String filter) {
        return fetchFilter(user, filter) == null;
    }

    protected List<QueryLiteral> convertToQueryLiteraCollection(final FunctionOperand operand,
            final Collection<Issue> issues) {
        List<QueryLiteral> result = new ArrayList<QueryLiteral>(issues.size());
        for (Issue issue : issues) {
            result.add(new QueryLiteral(operand, issue.getKey()));
        }
        return result;
    }

    private SharedEntitySearchParameters prepareSharedEntirySearchParameyer(String filter) {
        return new SharedEntitySearchParametersBuilder().setName(filter).toSearchParameters();

    }

    private SearchRequest tryToFetchById(User user, String filter) {
        if (!filter.matches("\\d+"))
            return null;
        return searchRequestService.getFilter(new JiraServiceContextImpl(user), Long.valueOf(filter));
    }

    private static class SearchRequestByNameConsumer implements Consumer<SearchRequest> {

        private String name;
        private SearchRequest found = null;

        public SearchRequestByNameConsumer(String name) {
            this.name = name;
        }

        public void consume(SearchRequest t) {
            if (t.getName().equals(name)) {
                found = t;
            }
        }

        public SearchRequest getFound() {
            return found;
        }
    }
}
