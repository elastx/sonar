/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.core.issue.db;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * @since 3.6
 */
public final class IssueChangeDto {

  private Long id;
  private String kee;
  private String issueKey;
  private String userLogin;
  private String changeType;
  private String changeData;
  private Date createdAt;
  private Date updatedAt;

  public Long getId() {
    return id;
  }

  public IssueChangeDto setId(Long id) {
    this.id = id;
    return this;
  }

  public String getKey() {
    return kee;
  }

  public IssueChangeDto setKey(String key) {
    this.kee = key;
    return this;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public IssueChangeDto setIssueKey(String issueKey) {
    this.issueKey = issueKey;
    return this;
  }

  public String getUserLogin() {
    return userLogin;
  }

  public IssueChangeDto setUserLogin(@Nullable String userLogin) {
    this.userLogin = userLogin;
    return this;
  }

  public String getChangeType() {
    return changeType;
  }

  public IssueChangeDto setChangeType(String changeType) {
    this.changeType = changeType;
    return this;
  }

  public String getChangeData() {
    return changeData;
  }

  public IssueChangeDto setChangeData(String changeData) {
    this.changeData = changeData;
    return this;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public IssueChangeDto setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public IssueChangeDto setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
