/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.batch.variables;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.AbstractBatchJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.json.JsonObjectConverter;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchSetVariablesHandler extends AbstractBatchJobHandler<BatchConfiguration> {

  public static final BatchJobDeclaration JOB_DECLARATION =
      new BatchJobDeclaration(Batch.TYPE_SET_VARIABLES);

  @Override
  public void execute(BatchJobConfiguration configuration,
                      ExecutionEntity execution,
                      CommandContext commandContext,
                      String tenantId) {

    String byteArrayId = configuration.getConfigurationByteArrayId();
    ByteArrayEntity byteArray = findByteArrayById(byteArrayId, commandContext);

    byte[] configurationByteArray = byteArray.getBytes();
    BatchConfiguration batchConfiguration = readConfiguration(configurationByteArray);

    String batchId = batchConfiguration.getBatchId();
    List<VariableInstanceEntity> variableInstances = commandContext.getVariableInstanceManager()
        .findVariableInstancesByBatchId(batchId);

    Map<String, Object> variables = variableInstances.stream()
        .collect(Collectors.toMap(VariableInstanceEntity::getName,
            VariableInstanceEntity::getTypedValueWithImplicitUpdatesSkipped));

    List<String> processInstanceIds = batchConfiguration.getIds();

    boolean initialLegacyRestrictions =
        commandContext.isRestrictUserOperationLogToAuthenticatedUsers();

    commandContext.disableUserOperationLog();
    commandContext.setRestrictUserOperationLogToAuthenticatedUsers(true);

    try {
      for (String processInstanceId : processInstanceIds) {
          commandContext.getProcessEngineConfiguration()
              .getRuntimeService()
              .setVariables(processInstanceId, variables);
      }

    } finally {
      commandContext.enableUserOperationLog();
      commandContext.setRestrictUserOperationLogToAuthenticatedUsers(initialLegacyRestrictions);

    }

    commandContext.getByteArrayManager().delete(byteArray);
  }

  @Override
  public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
    return JOB_DECLARATION;
  }

  @Override
  protected BatchConfiguration createJobConfiguration(BatchConfiguration configuration,
                                                      List<String> processIdsForJob) {
    return new BatchConfiguration(processIdsForJob);
  }

  @Override
  protected JsonObjectConverter<BatchConfiguration> getJsonConverterInstance() {
    return SetVariablesJsonConverter.INSTANCE;
  }

  @Override
  public String getType() {
    return Batch.TYPE_SET_VARIABLES;
  }

  protected ByteArrayEntity findByteArrayById(String byteArrayId, CommandContext commandContext) {
    return commandContext.getDbEntityManager()
        .selectById(ByteArrayEntity.class, byteArrayId);
  }

}
