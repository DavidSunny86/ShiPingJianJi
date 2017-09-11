precision highp float;
 uniform sampler2D inputImageTexture;
 varying highp vec2 textureCoordinate;

 uniform float level;

 void main(){

     vec3 centralColor;
     float sampleColor;


     vec2 blurCoordinates[20];

     float mul = 2.0;

     float mul_x = mul / 1280.0;
     float mul_y = mul / 720.0;


     blurCoordinates[0] = textureCoordinate + vec2(0.0 * mul_x,-10.0 * mul_y);
     blurCoordinates[1] = textureCoordinate + vec2(5.0 * mul_x,-8.0 * mul_y);
     blurCoordinates[2] = textureCoordinate + vec2(8.0 * mul_x,-5.0 * mul_y);
     blurCoordinates[3] = textureCoordinate + vec2(10.0 * mul_x,0.0 * mul_y);
     blurCoordinates[4] = textureCoordinate + vec2(8.0 * mul_x,5.0 * mul_y);
     blurCoordinates[5] = textureCoordinate + vec2(5.0 * mul_x,8.0 * mul_y);
     blurCoordinates[6] = textureCoordinate + vec2(0.0 * mul_x,10.0 * mul_y);
     blurCoordinates[7] = textureCoordinate + vec2(-5.0 * mul_x,8.0 * mul_y);
     blurCoordinates[8] = textureCoordinate + vec2(-8.0 * mul_x,5.0 * mul_y);
     blurCoordinates[9] = textureCoordinate + vec2(-10.0 * mul_x,0.0 * mul_y);
     blurCoordinates[10] = textureCoordinate + vec2(-8.0 * mul_x,-5.0 * mul_y);
     blurCoordinates[11] = textureCoordinate + vec2(-5.0 * mul_x,-8.0 * mul_y);
     blurCoordinates[12] = textureCoordinate + vec2(0.0 * mul_x,-6.0 * mul_y);
     blurCoordinates[13] = textureCoordinate + vec2(-4.0 * mul_x,-4.0 * mul_y);
     blurCoordinates[14] = textureCoordinate + vec2(-6.0 * mul_x,0.0 * mul_y);
     blurCoordinates[15] = textureCoordinate + vec2(-4.0 * mul_x,4.0 * mul_y);
     blurCoordinates[16] = textureCoordinate + vec2(0.0 * mul_x,6.0 * mul_y);
     blurCoordinates[17] = textureCoordinate + vec2(4.0 * mul_x,4.0 * mul_y);
     blurCoordinates[18] = textureCoordinate + vec2(6.0 * mul_x,0.0 * mul_y);
     blurCoordinates[19] = textureCoordinate + vec2(4.0 * mul_x,-4.0 * mul_y);


     sampleColor = texture2D(inputImageTexture, textureCoordinate).g * 22.0;

     sampleColor += texture2D(inputImageTexture, blurCoordinates[0]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[1]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[2]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[3]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[4]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[5]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[6]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[7]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[8]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[9]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[10]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[11]).g;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[12]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[13]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[14]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[15]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[16]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[17]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[18]).g * 2.0;
     sampleColor += texture2D(inputImageTexture, blurCoordinates[19]).g * 2.0;



     sampleColor = sampleColor/50.0;


     centralColor = texture2D(inputImageTexture, textureCoordinate).rgb;

     float dis = centralColor.g - sampleColor + 0.5;


     if(dis <= 0.5)
     {
         dis = dis * dis * 2.0;
     }
     else
     {
         dis = 1.0 - ((1.0 - dis)*(1.0 - dis) * 2.0);
     }

     if(dis <= 0.5)
     {
         dis = dis * dis * 2.0;
     }
     else
     {
         dis = 1.0 - ((1.0 - dis)*(1.0 - dis) * 2.0);
     }

     if(dis <= 0.5)
     {
         dis = dis * dis * 2.0;
     }
     else
     {
         dis = 1.0 - ((1.0 - dis)*(1.0 - dis) * 2.0);
     }

     if(dis <= 0.5)
     {
         dis = dis * dis * 2.0;
     }
     else
     {
         dis = 1.0 - ((1.0 - dis)*(1.0 - dis) * 2.0);
     }

     if(dis <= 0.5)
     {
         dis = dis * dis * 2.0;
     }
     else
     {
         dis = 1.0 - ((1.0 - dis)*(1.0 - dis) * 2.0);
     }
     // level 1:
     float smooth_para;
     float hue_para;
     float rou_para;
     float sat_para;
     if(abs(level - 1.0) <= 0.00001)
     {
         // level 1:
        smooth_para = 1.0;
        hue_para = 1.0;
        rou_para = 0.15;
        sat_para = 0.15;
     }else if(abs( level - 2.0) <= 0.00001)
     {
//         //leve 2:
         smooth_para = 0.8;
         hue_para = 0.6;
         rou_para = 0.25;
         sat_para = 0.25;
     }
     else if(abs( level - 3.0) <= 0.00001)
     {
//         //leve 3:
        smooth_para = 0.63;
        hue_para = 0.33;
        rou_para = 0.4;
        sat_para = 0.35;
     }
//


     float aa= 1.03;
     vec3 smoothColor = centralColor*aa - vec3(dis)*(aa-1.0);

     float hue = dot(smoothColor, vec3(0.299,0.587,0.114));

     aa = 1.0 + pow(hue, hue_para)*0.1;
     smoothColor = centralColor*aa - vec3(dis)*(aa-1.0);

     smoothColor.r = clamp(pow(smoothColor.r, smooth_para),0.0,1.0);
     smoothColor.g = clamp(pow(smoothColor.g, smooth_para),0.0,1.0);
     smoothColor.b = clamp(pow(smoothColor.b, smooth_para),0.0,1.0);


     vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);
     vec3 bianliang = max(smoothColor, centralColor);
     vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;


     gl_FragColor = vec4(mix(centralColor, lvse, pow(hue, hue_para)), 1.0);
     gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, pow(hue, hue_para));
     gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, rou_para);



     mat3 saturateMatrix = mat3(
                                1.1102,
                                -0.0598,
                                -0.061,
                                -0.0774,
                                1.0826,
                                -0.1186,
                                -0.0228,
                                -0.0228,
                                1.1772);

     vec3 satcolor = gl_FragColor.rgb * saturateMatrix;
     gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, sat_para);


 }