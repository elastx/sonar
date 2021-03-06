#
# Sonar, entreprise quality control tool.
# Copyright (C) 2008-2013 SonarSource
# mailto:contact AT sonarsource DOT com
#
# SonarQube is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# SonarQube is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
#
class Rule < ActiveRecord::Base

  MANUAL_REPOSITORY_KEY = 'manual'

  STATUS_READY = "READY"
  STATUS_BETA = "BETA"
  STATUS_DEPRECATED = "DEPRECATED"
  STATUS_REMOVED = "REMOVED"

  SORT_BY_RULE_NAME = "SORT_BY_RULE_NAME"
  SORT_BY_CREATION_DATE = "SORT_BY_CREATION_DATE"

  validates_presence_of :name, :description, :plugin_name
  validates_presence_of :plugin_rule_key, :if => 'name.present?'

  has_many :rules_parameters, :inverse_of => :rule
  has_many :rule_failures, :inverse_of => :rule
  has_many :active_rules, :inverse_of => :rule
  belongs_to :parent, :class_name => 'Rule', :foreign_key => 'parent_id'
  has_one :rule_note, :inverse_of => :rule
  alias_attribute :note, :rule_note

  def repository_key
    plugin_name
  end

  def parameters
    rules_parameters
  end

  def parameter(name)
    result=nil
    parameters.each do |param|
      result = param if param.name==name
    end
    result
  end

  def priority_text
    Sonar::RulePriority.to_s(priority)
  end

  def key
    "#{plugin_name}:#{plugin_rule_key}"
  end

  def template?
    cardinality=='MULTIPLE'
  end

  def ready?
    status == STATUS_READY
  end

  def editable?
    !parent_id.nil?
  end

  def enabled?
    !removed?
  end

  def disabled?
    removed?
  end

  def removed?
    status == STATUS_REMOVED
  end

  def deprecated?
    status == STATUS_DEPRECATED
  end

  def beta?
    status == STATUS_BETA
  end

  def <=>(other)
    return -1 if other.nil?
    return 1 if other.name.nil?
    name.downcase<=>other.name.downcase
  end

  def name(l10n=true)
    if l10n
      @l10n_name ||=
        begin
          result = Java::OrgSonarServerUi::JRubyFacade.getInstance().getRuleName(I18n.locale, repository_key, plugin_rule_key)
          result = read_attribute(:name) unless result
          result
        end
    else
      @raw_name ||=
        begin
          result = Java::OrgSonarServerUi::JRubyFacade.getInstance().getRuleName("en", repository_key, plugin_rule_key)
          # if no name present in the bundle, try to find it in the DB
          result = read_attribute(:name) unless result
          result
        end
    end
  end

  def name=(value)
    write_attribute(:name, value)
  end

  def description
    @l10n_description ||=
      begin
        result = Java::OrgSonarServerUi::JRubyFacade.getInstance().getRuleDescription(I18n.locale, repository_key, plugin_rule_key)
        result = read_attribute(:description) unless result
        result
      end
  end

  def description=(value)
    write_attribute(:description, value)
  end

  def config_key
    plugin_config_key
  end

  def self.to_i(key_or_id)
    id=key_or_id.to_i
    if id<=0 && key_or_id
      parts=key_or_id.split(':')
      if parts.size==2
        rule=Rule.first(:conditions => {:plugin_name => parts[0], :plugin_rule_key => parts[1]})
        id=rule.id if rule
      end
    end
    id>0 ? id : nil
  end

  def self.by_key_or_id(key_or_id)
    rule=nil
    if key_or_id.present?
      id=key_or_id.to_i
      if id<=0
        parts=key_or_id.split(':')
        if parts.size==2
          rule=Rule.first(:conditions => {:plugin_name => parts[0], :plugin_rule_key => parts[1]})
        end
      else
        rule=Rule.find(id)
      end
    end
    rule
  end

  def self.manual_rules
    rules = Rule.all(:conditions => ['status=? and plugin_name=?', STATUS_READY, MANUAL_REPOSITORY_KEY])
    Api::Utils.insensitive_sort(rules) { |rule| rule.name }
  end

  def self.manual_rule(id)
    Rule.first(:conditions => ['status=? and plugin_name=? and id=?', STATUS_READY, MANUAL_REPOSITORY_KEY, id])
  end

  def self.find_or_create_manual_rule(rule_id_or_name, create_if_not_found=false, options={})
    if Api::Utils.is_integer?(rule_id_or_name)
      rule = Rule.first(:conditions => {:status => STATUS_READY, :plugin_name => MANUAL_REPOSITORY_KEY, :id => rule_id_or_name.to_i})
    else
      key = rule_id_or_name.strip.downcase.sub(/\s+/, '_')
      rule = Rule.first(:conditions => {:status => STATUS_READY, :plugin_name => MANUAL_REPOSITORY_KEY, :plugin_rule_key => key})
      if rule==nil && create_if_not_found
        description = options[:description] || Api::Utils.message('manual_rules.should_provide_real_description')
        rule = Rule.create!(:status => STATUS_READY, :plugin_name => MANUAL_REPOSITORY_KEY, :plugin_rule_key => key,
                            :name => rule_id_or_name, :description => description)
      end
    end
    rule
  end

  def create_violation!(resource, options={})
    line = options['line']
    checksum = nil
    level = Sonar::RulePriority.id(options['severity']||Severity::MAJOR)
    RuleFailure.create!(
      :snapshot => resource.last_snapshot,
      :rule => self,
      :failure_level => level,
      :message => options['message'],
      :cost => (options['cost'] ? options['cost'].to_f : nil),
      :switched_off => false,
      :line => line,
      :checksum => checksum)
  end


  def to_hash_json(profile, active_rule = nil)
    json = {'title' => name, 'key' => key, 'plugin' => plugin_name, 'config_key' => config_key}
    json['description'] = description if description
    if profile
      active_rule = active_rule || profile.active_by_rule_id(id)
      if active_rule
        json['priority'] = active_rule.priority_text
        json['status'] = 'ACTIVE'
      else
        json['priority'] = priority_text
        json['status'] = 'INACTIVE'
      end
    else
      json['priority'] = priority_text
    end
    json['params'] = parameters.collect { |parameter| parameter.to_hash_json(active_rule) } unless parameters.empty?
    json
  end

  def to_xml(profile, xml)
    xml.rule do
      xml.title(name)
      xml.key(key)
      xml.config_key(config_key)
      xml.plugin(plugin_name)
      xml.description { xml.cdata!(description) } if description
      active_rule = nil
      if profile
        active_rule = profile.active_by_rule_id(id)
        if active_rule
          xml.priority(active_rule.priority_text)
          xml.status('ACTIVE')
        else
          xml.priority(priority_text)
          xml.status("INACTIVE")
        end
      else
        xml.priority(priority_text)
      end
      parameters.each do |parameter|
        parameter.to_xml(active_rule, xml)
      end
    end
  end

  def to_csv(profile)
    csv = [name.strip, plugin_rule_key, plugin_name]
    if profile
      active_rule = profile.active_by_rule_id(id)
      if active_rule
        csv << active_rule.priority_text
        csv << 'ACTIVE'
      else
        csv << priority_text
        csv << 'INACTIVE'
      end
    end
    csv
  end


  # options :language => nil, :repositories => [], :searchtext => '', :profile => nil, :priorities => [], :activation => '', :status => [], :sort_by => nil
  def self.search(java_facade, options={})
    conditions = []
    values = {}

    # For retro compatibility for the Sqale plugin
    unless options[:plugins].blank?
      options[:repositories] = options[:plugins]
    end

    status = options[:status]
    if status.blank?
      conditions << ['status <> :status']
      values[:status] = STATUS_REMOVED
    elsif status == 'ACTIVE' || status == 'INACTIVE'
      # For retro compatibility for the Sqale plugin
      options[:activation] = status
    else
      conditions << "status IN (:status)"
      values[:status] = options[:status]
    end

    unless options[:language].blank?
      conditions << ['language = :language']
      values[:language] = options[:language]
    end

    if remove_blank(options[:repositories])
      conditions << "plugin_name IN (:plugin_names)"
      values[:plugin_names] = options[:repositories]
    end

    unless options[:searchtext].blank?
      searchtext = options[:searchtext].to_s.strip
      search_text_conditions='(UPPER(rules.name) LIKE :searchtext OR plugin_rule_key = :key'

      additional_keys=java_facade.searchRuleName(I18n.locale, searchtext)
      additional_keys.each do |java_rule_key|
        search_text_conditions<<" OR (plugin_name='#{java_rule_key.getRepositoryKey()}' AND plugin_rule_key='#{java_rule_key.getKey()}')"
      end

      search_text_conditions<<')'
      conditions << search_text_conditions
      values[:searchtext] = "%" << searchtext.clone.upcase << "%"
      values[:key] = searchtext
    end

    includes=(options[:include_parameters_and_notes] ? [:rules_parameters, :rule_note] : nil)
    rules = Rule.all(:include => includes, :conditions => [conditions.join(" AND "), values])
    rules = Rule.sort_by(rules, options[:sort_by])
    filter(rules, options)
  end

  def self.remove_blank(array)
    if array
      array = array - ['']
      array.empty? ? nil : array
    else
      nil
    end
  end

  def self.filter(rules, options)
    priorities = remove_blank(options[:priorities])
    profile = options[:profile]
    inheritance = options[:inheritance]

    if profile
      inactive = (options[:activation]=='INACTIVE')
      active = (options[:activation]=='ACTIVE')

      rules = rules.reject do |rule|
        active_rule = profile.active_by_rule_id(rule.id)
        ((inactive && active_rule) || (active && active_rule.nil?))
      end

      if priorities
        rules = rules.select do |rule|
          active_rule = profile.active_by_rule_id(rule.id)
          (active_rule && priorities.include?(active_rule.priority_text)) || (active_rule.nil? && priorities.include?(rule.priority_text))
        end
      end

      if inheritance=='NOT'
        rules = rules.select do |rule|
          active_rule = profile.active_by_rule_id(rule.id)
          (active_rule.nil? || active_rule.inheritance.blank?)
        end
      elsif inheritance.present? && inheritance != 'any'
        rules = rules.select do |rule|
          active_rule = profile.active_by_rule_id(rule.id)
          (active_rule && active_rule.inheritance==inheritance)
        end
      end

    elsif priorities
      rules = rules.select do |rule|
        priorities.include?(rule.priority_text)
      end
    end
    rules
  end

  def self.sort_by(rules, sort_by)
    case sort_by
      when SORT_BY_CREATION_DATE
        rules = rules.sort_by {|rule| rule.created_at}.reverse
      else
        rules = rules.sort
    end
    rules
  end

end
