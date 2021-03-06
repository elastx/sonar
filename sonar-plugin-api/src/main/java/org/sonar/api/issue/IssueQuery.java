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

package org.sonar.api.issue;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.sonar.api.rule.RuleKey;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * TODO add javadoc
 *
 * @since 3.6
 */
public class IssueQuery {

  private final Collection<String> issueKeys;
  private final Collection<String> severities;
  private final Collection<String> statuses;
  private final Collection<String> resolutions;
  private final Collection<String> components;
  private final Collection<String> componentRoots;
  private final Collection<RuleKey> rules;
  private final Collection<String> userLogins;
  private final Collection<String> assignees;
  private final Boolean assigned;
  private final Date createdAfter;
  private final Date createdBefore;
  private final String sort;
  private final boolean asc;

  // max results per page
  private final int pageSize;

  // index of selected page. Start with 1.
  private final int pageIndex;

  private IssueQuery(Builder builder) {
    this.issueKeys = builder.issueKeys;
    this.severities = builder.severities;
    this.statuses = builder.statuses;
    this.resolutions = builder.resolutions;
    this.components = builder.components;
    this.componentRoots = builder.componentRoots;
    this.rules = builder.rules;
    this.userLogins = builder.userLogins;
    this.assignees = builder.assignees;
    this.assigned = builder.assigned;
    this.createdAfter = builder.createdAfter;
    this.createdBefore = builder.createdBefore;
    this.sort = builder.sort;
    this.asc = builder.asc;
    this.pageSize = builder.pageSize;
    this.pageIndex = builder.pageIndex;
  }

  public Collection<String> issueKeys() {
    return issueKeys;
  }

  public Collection<String> severities() {
    return severities;
  }

  public Collection<String> statuses() {
    return statuses;
  }

  public Collection<String> resolutions() {
    return resolutions;
  }

  public Collection<String> components() {
    return components;
  }

  public Collection<String> componentRoots() {
    return componentRoots;
  }

  public Collection<RuleKey> rules() {
    return rules;
  }

  public Collection<String> userLogins() {
    return userLogins;
  }

  public Collection<String> assignees() {
    return assignees;
  }

  public Boolean assigned() {
    return assigned;
  }

  public Date createdAfter() {
    return createdAfter;
  }

  public Date createdBefore() {
    return createdBefore;
  }

  public String sort() {
    return sort;
  }

  public boolean asc() {
    return asc;
  }

  public int pageSize() {
    return pageSize;
  }

  public int pageIndex() {
    return pageIndex;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }

  public static Builder builder() {
    return new Builder();
  }


  /**
   * @since 3.6
   */
  public static class Builder {

    private enum Sort {
      created, updated, closed, assignee;
    }

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 1000;
    private static final int DEFAULT_PAGE_INDEX = 1;

    private Collection<String> issueKeys;
    private Collection<String> severities;
    private Collection<String> statuses;
    private Collection<String> resolutions;
    private Collection<String> components;
    private Collection<String> componentRoots;
    private Collection<RuleKey> rules;
    private Collection<String> userLogins;
    private Collection<String> assignees;
    private Boolean assigned = null;
    private Date createdAfter;
    private Date createdBefore;
    private String sort;
    private boolean asc = false;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int pageIndex = DEFAULT_PAGE_INDEX;

    private Builder() {
    }

    public Builder issueKeys(Collection<String> l) {
      this.issueKeys = l;
      return this;
    }

    public Builder severities(Collection<String> l) {
      this.severities = l;
      return this;
    }

    public Builder statuses(Collection<String> l) {
      this.statuses = l;
      return this;
    }

    public Builder resolutions(Collection<String> l) {
      this.resolutions = l;
      return this;
    }

    public Builder components(Collection<String> l) {
      this.components = l;
      return this;
    }

    public Builder componentRoots(Collection<String> l) {
      this.componentRoots = l;
      return this;
    }

    public Builder rules(Collection<RuleKey> rules) {
      this.rules = rules;
      return this;
    }

    public Builder userLogins(Collection<String> l) {
      this.userLogins = l;
      return this;
    }

    public Builder assignees(Collection<String> l) {
      this.assignees = l;
      return this;
    }

    /**
     * If true, it will return all issues assigned to someone
     * If false, it will return all issues not assigned to someone
     */
    public Builder assigned(Boolean assigned) {
      this.assigned = assigned;
      return this;
    }

    public Builder createdAfter(Date createdAfter) {
      this.createdAfter = createdAfter;
      return this;
    }

    public Builder createdBefore(Date createdBefore) {
      this.createdBefore = createdBefore;
      return this;
    }

    public Builder sort(@Nullable String sort) {
      if (sort != null) {
        try {
          this.sort = Sort.valueOf(sort).name();
        } catch (IllegalArgumentException e){
          throw new IllegalArgumentException("Sort should contain only : " + Arrays.toString(Sort.values()), e);
        }
      }
      return this;
    }

    public Builder asc(boolean asc) {
      this.asc = asc;
      return this;
    }

    public Builder pageSize(@Nullable Integer i) {
      Preconditions.checkArgument(i == null || i.intValue() > 0, "Page size must be greater than 0 (got " + i + ")");
      Preconditions.checkArgument(i == null || i.intValue() < MAX_PAGE_SIZE, "Page size must be less than " + MAX_PAGE_SIZE + " (got " + i + ")");
      this.pageSize = (i == null ? DEFAULT_PAGE_SIZE : i.intValue());
      return this;
    }

    public Builder pageIndex(@Nullable Integer i) {
      Preconditions.checkArgument(i == null || i.intValue() > 0, "Page index must be greater than 0 (got " + i + ")");
      this.pageIndex = (i == null ? DEFAULT_PAGE_INDEX : i.intValue());
      return this;
    }

    public IssueQuery build() {
      return new IssueQuery(this);
    }
  }
}
