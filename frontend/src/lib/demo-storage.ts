import { Preferences } from "@capacitor/preferences";

// Somente dados ficticios. Nao armazenar senhas, tokens ou dados financeiros reais.
export const demoStorage = {
  async read<T>(key: string): Promise<T | null> {
    const { value } = await Preferences.get({ key: `cash-demo:${key}` });
    return value === null ? null : JSON.parse(value) as T;
  },
  async write<T>(key: string, value: T): Promise<void> {
    await Preferences.set({ key: `cash-demo:${key}`, value: JSON.stringify(value) });
  },
  async remove(key: string): Promise<void> {
    await Preferences.remove({ key: `cash-demo:${key}` });
  },
};
