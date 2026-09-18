<script>
	/**
	 * @typedef {Object} Props
	 * @property {string|null} [message]
	 * @property {() => void} [onretry]
	 * @property {() => void} [ondismiss]
	 * @property {boolean} [dismissible]
	 */

	/** @type {Props} */
	let {
		message = null,
		onretry = null,
		ondismiss = null,
		dismissible = true
	} = $props();

	let dismissed = $state(false);

	function handleDismiss() {
		dismissed = true;
		if (ondismiss) ondismiss();
	}
</script>

{#if message && !dismissed}
	<div
		class="rounded-xl border border-rose-200 bg-rose-50/90 p-4 text-rose-900 shadow-xs mb-6 flex items-start gap-3.5 transition-all"
		role="alert"
	>
		<div class="rounded-full bg-rose-100 p-1 text-rose-600 shrink-0 mt-0.5">
			<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
				<path
					stroke-linecap="round"
					stroke-linejoin="round"
					stroke-width="2"
					d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
				/>
			</svg>
		</div>

		<div class="flex-1">
			<h4 class="text-sm font-semibold text-rose-950">Action Failed</h4>
			<p class="mt-0.5 text-sm text-rose-800 leading-relaxed">
				{message}
			</p>
			{#if onretry}
				<div class="mt-2.5">
					<button
						type="button"
						class="text-xs font-semibold text-rose-700 hover:text-rose-900 underline underline-offset-2 inline-flex items-center gap-1"
						onclick={onretry}
					>
						<span>Try again</span>
						<svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
						</svg>
					</button>
				</div>
			{/if}
		</div>

		{#if dismissible}
			<button
				type="button"
				class="text-rose-400 hover:text-rose-600 rounded-md p-1 -mr-1 -mt-1 hover:bg-rose-100 transition-colors"
				onclick={handleDismiss}
				aria-label="Dismiss alert"
			>
				<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
				</svg>
			</button>
		{/if}
	</div>
{/if}
