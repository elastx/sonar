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
package org.sonar.core.properties;

import org.apache.ibatis.annotations.Param;

import javax.annotation.Nullable;

import java.util.List;

public interface PropertiesMapper {

  List<String> findUsersForNotification(@Param("notifKey") String notificationKey, @Nullable @Param("rId") Long resourceId);

  List<PropertyDto> selectGlobalProperties();

  List<PropertyDto> selectProjectProperties(String resourceKey);

  List<PropertyDto> selectSetOfResourceProperties(@Param("rId") Long projectId, @Param("propKeys") List<String> propertyKeys);

  PropertyDto selectByKey(PropertyDto key);

  void update(PropertyDto property);

  void insert(PropertyDto property);

  void deleteGlobalProperty(String key);

  void deleteGlobalProperties();

  void renamePropertyKey(@Param("oldKey") String oldKey, @Param("newKey") String newKey);
}
