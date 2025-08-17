/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.hertssu.security;

import java.util.Map;

 /**
 *
 * @author user
 */
public final class RoleRank {
  public static final Map<String,Integer> RANK = Map.ofEntries(
    Map.entry("PRESIDENT", 0),
    Map.entry("VICE PRESIDENT", 1),
    Map.entry("EXECUTIVE OFFICER", 2),
    Map.entry("HR CHAIRPERSON", 3),
    Map.entry("TREASURER", 3),
    Map.entry("CHAIRPERSON", 3),    
    Map.entry("ASSOCIATE CHAIRPERSON", 4),
    Map.entry("LEADER", 5),
    Map.entry("OFFICER", 6),
    Map.entry("MEMBER", 7)
  );

  public static int rankOf(String role){
    if (role == null) return Integer.MAX_VALUE;
    return RANK.getOrDefault(role.toUpperCase(), 999);
  }
}
