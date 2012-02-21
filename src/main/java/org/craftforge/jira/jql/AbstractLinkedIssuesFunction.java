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
package org.craftforge.jira.jql;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import java.util.*;
import org.craftforge.jira.jql.linkedissues.collectors.InwardLinkedIssueCollector;
import org.craftforge.jira.jql.linkedissues.collectors.LinkedIssueCollector;
import org.craftforge.jira.jql.linkedissues.collectors.NoSpecificLinkedIssueCollector;
import org.craftforge.jira.jql.linkedissues.collectors.OutwardLinkedIssueCollector;

/**
 *
 * @author pbojko
 */
public abstract class AbstractLinkedIssuesFunction extends AbstractIssuesFromFilterFunction {

	private IssueLinkManager issueLinkManager;
	
	private Collection<LinkedIssueCollector> collectors = new LinkedList<LinkedIssueCollector>();

	public AbstractLinkedIssuesFunction() {
		collectors.add(new NoSpecificLinkedIssueCollector());
		collectors.add(new InwardLinkedIssueCollector());
		collectors.add(new OutwardLinkedIssueCollector());
	}
	
	@Override
	protected final void init(JqlFunctionModuleDescriptor moduleDescriptor, ComponentManager componentManager) {
		issueLinkManager = componentManager.getIssueLinkManager();
	}

	public final int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	public final List<QueryLiteral> getValues(QueryCreationContext qcc, FunctionOperand fo, TerminalClause tc) {
		List<Issue> issues = findIssues(qcc, fo);
		String relationNameOrNull = fetchParameter(fo, 1);
		String relationDirectionOrNull = fetchParameter(fo, 2);
		issues = fetchLinkedIssues(qcc.getUser(), issues, relationNameOrNull, relationDirectionOrNull);
		return convertToQueryLiteraCollection(fo, issues);
	}

	
	private String fetchParameter(FunctionOperand fo, int index) {
		return fo.getArgs().size() > index ? fo.getArgs().get(index) : null;
	}

	private List<Issue> fetchLinkedIssues(User user, List<Issue> issues, String relation, String direction) {
		List<Issue> result = new ArrayList<Issue>(issues.size());
		for (Issue issue : issues) {
			LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, user);
			for (LinkedIssueCollector collector : collectors) {
				Collection<Issue> collection = collector.collectIfNeeded(relation, direction,linkCollection);
				result.addAll(nullSafeCollection(collection));
			}
		}
		return result;
	}

	private Collection<Issue> nullSafeCollection(Collection<Issue> issues) {
		return issues == null ? Collections.<Issue>emptyList() : issues;
	}
	
}
