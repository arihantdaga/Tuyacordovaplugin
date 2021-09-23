
@implementation TyMiscUtils

#pragma mark - Color Manipulation

+ (UIColor *) stringToColor:(NSString *) color {
    if([color length] != 7) {
        return nil;
    }
    NSString *color1 = [color substringFromIndex:1];

    NSScanner *scanner = [NSScanner scannerWithString:color1];
    UInt64 hexNumber = 0;
    if ([scanner scanHexLongLong:&hexNumber]) {
        CGFloat r = ((hexNumber & 0xff0000) >> 16) / 255.0;
        CGFloat g = ((hexNumber & 0x00ff00) >> 8) / 255.0;
        CGFloat b = ((hexNumber & 0x0000ff)) / 255.0;
        CGFloat a = 1.0;

        return [UIColor colorWithRed:r green:g blue:b alpha:a];
    }

    return [UIColor whiteColor];
}

@end
