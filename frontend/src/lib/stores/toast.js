import { writable } from 'svelte/store';

function createToastStore() {
	const { subscribe, update } = writable([]);

	/**
	 * Add a toast notification
	 * @param {string} message
	 * @param {'success'|'error'|'info'|'warning'} type
	 * @param {number} timeoutMs
	 */
	function add(message, type = 'info', timeoutMs = 4500) {
		const id = Date.now() + Math.random().toString(36).substring(2, 7);
		const toast = { id, message, type };

		update((items) => [...items, toast]);

		if (timeoutMs > 0) {
			setTimeout(() => {
				remove(id);
			}, timeoutMs);
		}
		return id;
	}

	function remove(id) {
		update((items) => items.filter((t) => t.id !== id));
	}

	return {
		subscribe,
		add,
		remove,
		success: (msg, timeout) => add(msg, 'success', timeout),
		error: (msg, timeout) => add(msg, 'error', timeout),
		info: (msg, timeout) => add(msg, 'info', timeout),
		warning: (msg, timeout) => add(msg, 'warning', timeout)
	};
}

export const toast = createToastStore();
