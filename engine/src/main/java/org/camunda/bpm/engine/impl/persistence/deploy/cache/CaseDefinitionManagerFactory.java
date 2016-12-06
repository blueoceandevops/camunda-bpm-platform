/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.persistence.deploy.cache;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.AbstractResourceDefinitionManager;

/**
 * @author: Johannes Heinemann
 */
public class CaseDefinitionManagerFactory implements DefinitionManagerFactory<CaseDefinitionEntity> {

  @Override
  @SuppressWarnings("ConstantConditions")
  public AbstractResourceDefinitionManager<CaseDefinitionEntity> getManager() {
    return Context.getCommandContext().getCaseDefinitionManager();
  }
}