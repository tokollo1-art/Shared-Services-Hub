<script>
	/**
	 * @typedef {Object} Props
	 * @property {number} [rating]
	 * @property {boolean} [interactive]
	 * @property {number} [max]
	 * @property {'sm'|'md'|'lg'} [size]
	 * @property {(newRating: number) => void} [onchange]
	 */

	/** @type {Props} */
	let {
		rating = $bindable(0),
		interactive = false,
		max = 5,
		size = 'md',
		onchange = () => {}
	} = $props();

	let hoverRating = $state(0);

	const starSizes = $derived.by(() => {
		switch (size) {
			case 'sm':
				return 'text-sm gap-0.5';
			case 'lg':
				return 'text-2xl gap-1.5';
			default:
				return 'text-lg gap-1';
		}
	});

	function handleStarClick(index) {
		if (!interactive) return;
		rating = index;
		onchange(index);
	}

	function handleMouseEnter(index) {
		if (!interactive) return;
		hoverRating = index;
	}

	function handleMouseLeave() {
		if (!interactive) return;
		hoverRating = 0;
	}
</script>

<div
	class="inline-flex items-center select-none {starSizes}"
	role={interactive ? 'radiogroup' : 'img'}
	aria-label={`Rating: ${rating} out of ${max} stars`}
	onmouseleave={handleMouseLeave}
>
	{#each Array(max) as _, i}
		{@const starIndex = i + 1}
		{@const activeRating = hoverRating > 0 ? hoverRating : (rating || 0)}
		{@const isFilled = activeRating >= starIndex}
		{@const isHalf = !isFilled && activeRating >= starIndex - 0.5}

		{#if interactive}
			<button
				type="button"
				class="transition-transform hover:scale-110 focus:outline-none focus:ring-1 focus:ring-[#1446A0] p-0.5"
				onclick={() => handleStarClick(starIndex)}
				onmouseenter={() => handleMouseEnter(starIndex)}
				title={`${starIndex} star${starIndex > 1 ? 's' : ''}`}
				aria-label={`${starIndex} stars`}
			>
				{#if isFilled}
					<span style="color: #E8B830;">★</span>
				{:else}
					<span style="color: #D1D5DB;">☆</span>
				{/if}
			</button>
		{:else}
			<span class="transition-colors">
				{#if isFilled}
					<span style="color: #E8B830;">★</span>
				{:else if isHalf}
					<span style="color: #E8B830;">★</span>
				{:else}
					<span style="color: #D1D5DB;">☆</span>
				{/if}
			</span>
		{/if}
	{/each}

	{#if rating > 0 && !interactive}
		<span class="ml-1.5 font-mono font-semibold text-[#0D1B3E] {size === 'sm' ? 'text-xs' : 'text-sm'}">
			{Number(rating).toFixed(1)}
		</span>
	{/if}
</div>
