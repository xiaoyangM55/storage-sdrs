/*
 * Copyright 2019 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and limitations under the License.
 *
 * Any software provided by Google hereunder is distributed “AS IS”,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, and is not intended for production use.
 */

package com.google.gcs.sdrs.service.worker.rule.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gcs.sdrs.common.RetentionRuleType;
import com.google.gcs.sdrs.dao.model.RetentionJob;
import com.google.gcs.sdrs.dao.model.RetentionRule;
import com.google.gcs.sdrs.util.CredentialsUtil;
import com.google.gcs.sdrs.util.StsUtil;
import java.io.IOException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CredentialsUtil.class, StsUtil.class})
@PowerMockIgnore("javax.management.*")
public class StsRuleExecutorTest {

  private StsRuleExecutor objectUnderTest;
  private String dataStorageName = "gs://test";
  private RetentionRule testRule;
  private CredentialsUtil mockCredentialsUtil;

  @Before
  public void setup() throws IOException {
    testRule = new RetentionRule();
    testRule.setId(1);
    testRule.setProjectId("sdrs-test");
    testRule.setDatasetName("test");
    testRule.setRetentionValue("30:day");
    testRule.setDataStorageName(dataStorageName);
    testRule.setType(RetentionRuleType.DATASET);
    testRule.setVersion(2);

    mockCredentialsUtil = mock(CredentialsUtil.class);
    PowerMockito.mockStatic(CredentialsUtil.class);
    when(CredentialsUtil.getInstance()).thenReturn(mockCredentialsUtil);
    PowerMockito.mockStatic(StsUtil.class);
    when(StsUtil.createStsClient(any())).thenReturn(null);

    objectUnderTest = StsRuleExecutor.getInstance();
  }

  @Test
  public void globalRuleExecutionWithDatasetType() {
    try {
      Collection<RetentionRule> bucketRules = new HashSet<>();
      Collection<RetentionRule> defaultRules = new HashSet<>();
      ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
      objectUnderTest.executeDefaultRule(testRule, defaultRules, bucketRules, now, "project-id");
    } catch (IllegalArgumentException ex) {
      assertTrue(true);
    } catch (Exception ex) {
      Assert.fail();
    }
  }

  @Test
  public void buildRetentionJobTest() {
    String jobName = "test";

    RetentionJob result = objectUnderTest.buildRetentionJobEntity(jobName, testRule, null, null);

    assertEquals(result.getName(), jobName);
    assertEquals((int) result.getRetentionRuleId(), (int) testRule.getId());
    assertEquals(result.getRetentionRuleDataStorageName(), testRule.getDataStorageName());
    assertEquals(result.getRetentionRuleType(), testRule.getType());
    assertEquals((int) result.getRetentionRuleVersion(), (int) testRule.getVersion());
  }
}
