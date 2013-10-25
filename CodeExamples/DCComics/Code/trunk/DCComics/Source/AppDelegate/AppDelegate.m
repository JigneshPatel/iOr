//
//  AppDelegate.m
//  DCComics
//
//  Created by Krunal Doshi on 2/12/13.
//  Copyright (c) 2013 Krunal Doshi. All rights reserved.
//

#import "AppDelegate.h"
#import "LogInViewController.h"


@implementation AppDelegate

NSString * const kOAuthCallbackURL = @"egwfapi://auth";

- (void)dealloc
{
    [_window release];
    [_navigationController release];
    [super dealloc];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    [WFInstagramAPI setClientId:@"c3bba03fd31a4e8fa9e54ac595b2f15c"];
    [WFInstagramAPI setClientSecret:@"b1913d7e7a904ff2b22aa4ffc11ef288"];
    [WFInstagramAPI setOAuthRedirectURL:kOAuthCallbackURL];
    
    self.window = [[[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
    // Override point for customization after application launch.

    LogInViewController *loginViewController = [[[LogInViewController alloc] init] autorelease];
    self.navigationController = [[[UINavigationController alloc] initWithRootViewController:loginViewController] autorelease];
    self.window.rootViewController = self.navigationController;
    [self.window makeKeyAndVisible];
    return YES;
}
- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication
         annotation:(id)annotation {
    // FBSample logic
    // We need to handle URLs by passing them to FBSession in order for SSO authentication
    // to work.
    NSString *strURL = [url absoluteString];
    if ([strURL rangeOfString:@"code"].location == NSNotFound) {
    return [FBSession.activeSession handleOpenURL:url];
    }
    else{
        NSDictionary *params = [url queryDictionary];
        
        // make the request to get the user's token, then store it in defaults/synchronize & set it on API
        WFIGResponse *response = [WFInstagramAPI accessTokenForCode:[params objectForKey:@"code"]];
        NSDictionary *json = [response parsedBody];
        NSString *token = [json objectForKey:@"access_token"];
        [WFInstagramAPI setAccessToken:token];
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setObject:token forKey:@"user_token"];
        [defaults synchronize];
        
        // dismiss our auth controller, get back to the regular application
        UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
        [keyWindow resignKeyWindow];
        keyWindow.hidden = YES;
        [WFInstagramAPI setAuthWindow:nil];
        [self.window makeKeyAndVisible];
        
        return YES;
    }
}
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


@end
