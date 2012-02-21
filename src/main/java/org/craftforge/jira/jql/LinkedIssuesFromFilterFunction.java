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
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptor;
import org.craftforge.jira.jql.query.QueryFromFilterProvider;
import org.craftforge.jira.jql.query.QueryProvider;

/**
 *
 * @author pbojko
 */
public class LinkedIssuesFromFilterFunction extends AbstractLinkedIssuesFunction {

	@Override
	protected QueryProvider createQueryProvider(JqlFunctionModuleDescriptor moduleDescriptor, ComponentManager componentManager) {
		return new QueryFromFilterProvider(componentManager.getSearchRequestService() ,moduleDescriptor.getI18nBean());
	}

	@Override
	public String getFunctionName() {
		return "linkedIssuesFromFilter";
	}
}
