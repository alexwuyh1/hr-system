package com.example.hr.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

/**
 * Simple face hash service (MVP).
 * Uses average hash (aHash) for 1:1 similarity.
 */
@Service
public class FaceService {
  public String computeHash(InputStream inputStream) throws Exception {
    BufferedImage image = ImageIO.read(inputStream);
    if (image == null) {
      throw new IllegalArgumentException("Invalid image");
    }
    BufferedImage scaled = resizeToGray(image, 8, 8);
    int[] pixels = new int[64];
    int idx = 0;
    int total = 0;
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        int rgb = scaled.getRGB(x, y) & 0xFF;
        pixels[idx++] = rgb;
        total += rgb;
      }
    }
    int avg = total / 64;
    long hash = 0L;
    for (int i = 0; i < 64; i++) {
      if (pixels[i] >= avg) {
        hash |= (1L << i);
      }
    }
    return String.format("%016x", hash);
  }

  public int hammingDistance(String hashA, String hashB) {
    long a = Long.parseUnsignedLong(hashA, 16);
    long b = Long.parseUnsignedLong(hashB, 16);
    return Long.bitCount(a ^ b);
  }

  private BufferedImage resizeToGray(BufferedImage source, int w, int h) {
    Image tmp = source.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g2d = resized.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();
    return resized;
  }
}
