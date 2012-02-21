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
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.opensymphony.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.craftforge.jira.jql.query.QueryProvider;

/**
 *
 * @author pbojko
 */
public abstract class AbstractIssuesFromFilterFunction extends AbstractJqlFunction {
	
	private QueryProvider queryProvider;

	private SearchProvider searchProvider;

	private I18nHelper i18nHelper;

	public JiraDataType getDataType() {
		return JiraDataTypes.ISSUE;
	}

	@Override
	public final void init(JqlFunctionModuleDescriptor moduleDescriptor) {
		super.init(moduleDescriptor);
		ComponentManager componentManager = ComponentManager.getInstance();
		searchProvider = componentManager.getSearchProvider();
		i18nHelper = moduleDescriptor.getI18nBean();
		this.queryProvider = createQueryProvider(moduleDescriptor, componentManager);
		init(moduleDescriptor, componentManager);
	}
	
	protected abstract void init(JqlFunctionModuleDescriptor moduleDescriptor, ComponentManager componentManager);
	
	protected abstract QueryProvider createQueryProvider(JqlFunctionModuleDescriptor moduleDescriptor, ComponentManager componentManager);

	@Override
	public boolean isList() {
		return true;
	}

	@Override
	public MessageSet validate(com.opensymphony.user.User user, FunctionOperand operand, TerminalClause tc) {
		MessageSet messages = new MessageSetImpl();
		final List<String> args = operand.getArgs();
		if (args.isEmpty()) {
			messages.addErrorMessage(i18nHelper.getText("abstract-issues-from-filter.bad.num.arguments", operand.getName()));
		}
		return messages;
	}

	protected List<Issue> findIssues(QueryCreationContext qcc, FunctionOperand fo) {
		try {
			Query query = queryProvider.provide(qcc.getQueryUser(), fo.getArgs().get(0));
			List<Issue> issues = searchProvider.search(query, qcc.getQueryUser(), PagerFilter.getUnlimitedFilter()).getIssues();
			return issues;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected List<QueryLiteral> convertToQueryLiteraCollection(final FunctionOperand operand,
			final Collection<Issue> issues) {
		List<QueryLiteral> result = new ArrayList<QueryLiteral>(issues.size());
		for (Issue issue : issues) {
			result.add(new QueryLiteral(operand, issue.getKey()));
		}
		return result;
	}

}
