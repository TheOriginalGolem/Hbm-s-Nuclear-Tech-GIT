package com.hbm.handler.crt;

import java.util.List;
import java.util.ArrayList;

import com.hbm.inventory.RBMKOutgasserRecipes;
import crafttweaker.IAction;
import crafttweaker.CraftTweakerAPI;

public class NTMCraftTweaker {
    public static final List<IAction> postInitActions = new ArrayList<>();

    public static void applyPostInitActions(){
        try{
            postInitActions.forEach( CraftTweakerAPI::apply );
        } catch( final Throwable t ){
            System.out.println("CraftTweakerAPI not found");
        }
    }
}

