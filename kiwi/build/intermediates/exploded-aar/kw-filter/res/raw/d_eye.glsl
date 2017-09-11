 varying highp vec2 textureCoordinate;
             
             uniform sampler2D inputImageTexture;
             
             uniform highp float aspectRatio;
             uniform highp vec2 center;
             uniform highp vec2 newcenter;
             uniform highp float radius;
             uniform highp float scale;
            
             void main()
             {
            //    highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, (textureCoordinate.y * aspectRatio + 0.5 - 0.5 * aspectRatio));
                highp vec2 textureCoordinateToUse = vec2(textureCoordinate.x, textureCoordinate.y* aspectRatio);

                highp vec2  centerLeft = vec2(center.x, center.y* aspectRatio);
                highp vec2  centerRight = vec2(newcenter.x, newcenter.y* aspectRatio);

                highp float dist = distance(centerLeft, textureCoordinateToUse);
                highp float newdist = distance(centerRight, textureCoordinateToUse);
//                textureCoordinateToUse = textureCoordinate;
                
                if (dist < radius)
                {
                    textureCoordinateToUse -= centerLeft;
                    highp float percent = 1.0 - ((radius - dist) / radius) * scale;
                    percent = percent * percent;
                    
                    textureCoordinateToUse = textureCoordinateToUse * percent;
                    textureCoordinateToUse += centerLeft;
                }
                 if (newdist < radius)
                 {
                     textureCoordinateToUse -= centerRight;
                     highp float percent = 1.0 - ((radius - newdist) / radius) * scale;
                     percent = percent * percent;
                     
                     textureCoordinateToUse = textureCoordinateToUse * percent;
                     textureCoordinateToUse += centerRight;
                 }

                textureCoordinateToUse = vec2(textureCoordinateToUse.x, textureCoordinateToUse.y/aspectRatio);
                gl_FragColor = texture2D(inputImageTexture, textureCoordinateToUse );    
             }