package me.rebirthclient.api.util;

public class FadeUtils {
   protected long start;
   public long length;

   public FadeUtils(long ms) {
      this.length = ms;
      this.reset();
   }

   public void reset() {
      this.start = System.currentTimeMillis();
   }

   public boolean isEnd() {
      return this.getTime() >= this.length;
   }

   protected long getTime() {
      return System.currentTimeMillis() - this.start;
   }

   public void setLength(long length) {
      this.length = length;
   }

   private double getFadeOne() {
      return this.isEnd() ? 1.0 : (double)this.getTime() / (double)this.length;
   }

   public double getFadeInDefault() {
      return Math.tanh((double)this.getTime() / (double)this.length * 3.0);
   }

   public double getFadeOutDefault() {
      return 1.0 - Math.tanh((double)this.getTime() / (double)this.length * 3.0);
   }

   public double getEpsEzFadeIn() {
      return 1.0 - Math.sin((Math.PI / 2) * this.getFadeOne()) * Math.sin((Math.PI * 4.0 / 5.0) * this.getFadeOne());
   }

   public double getEpsEzFadeOut() {
      return Math.sin((Math.PI / 2) * this.getFadeOne()) * Math.sin((Math.PI * 4.0 / 5.0) * this.getFadeOne());
   }

   public double easeOutQuad() {
      return 1.0 - (1.0 - this.getFadeOne()) * (1.0 - this.getFadeOne());
   }

   public double easeInQuad() {
      return this.getFadeOne() * this.getFadeOne();
   }

   public double def() {
      return this.isEnd() ? 1.0 : this.getFadeOne();
   }
}
