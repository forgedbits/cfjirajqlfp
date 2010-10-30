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
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.opensymphony.user.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author pbojko
 */
public class LinkedIssuesFromFilterFunction extends AbstractIssuesFromFilterFunction {

	protected IssueLinkManager issueLinkManager;

	@Override
	public void init(JqlFunctionModuleDescriptor moduleDescriptor) {
		super.init(moduleDescriptor);
		issueLinkManager = ComponentManager.getInstance().getIssueLinkManager();
	}

	@Override
	public String getFunctionName() {
		return "linkedIssuesFromFilter";
	}
	
	public List<QueryLiteral> getValues(QueryCreationContext qcc, FunctionOperand fo, TerminalClause tc) {
		List<Issue> issues = fetchIssuesFromSubfilter(qcc, fo);
		String relationNameOrNull = fo.getArgs().size() > 1 ? fo.getArgs().get(1) : null;
		issues = fetchLinkedIssues(qcc.getUser(), issues, relationNameOrNull);
		return convertToQueryLiteraCollection(fo, issues);
	}

	public int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	private List<Issue> fetchLinkedIssues(User user, List<Issue> issues, String relation) {
		List<Issue> result = new ArrayList<Issue>(issues.size());
		for (Issue issue : issues) {
			LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, user);
			if (relation == null) {
				result.addAll(nullSafeCollection(linkCollection.getAllIssues()));
			} else {
				result.addAll(nullSafeCollection(linkCollection.getInwardIssues(relation)));
				result.addAll(nullSafeCollection(linkCollection.getOutwardIssues(relation)));
			}
		}
		return result;
	}
	
	private Collection<Issue> nullSafeCollection(Collection<Issue> issues) {
		return issues==null? Collections.<Issue>emptyList() : issues;
	}
}
